package github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.item.ItemList;
import github.kasuminova.mmce.client.util.ItemStackUtils;
import github.kasuminova.mmce.common.helper.IDynamicPatternInfo;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.client.util.BlockModelHider;
import github.kasuminova.ecoaeextension.common.block.ecotech.efabricator.BlockEFabricatorController;
import github.kasuminova.ecoaeextension.common.block.ecotech.efabricator.prop.Levels;
import github.kasuminova.ecoaeextension.common.network.PktEFabricatorGUIData;
import github.kasuminova.ecoaeextension.common.tile.ecotech.EPartController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorParallelProc.Modifier;
import github.kasuminova.ecoaeextension.common.util.MachineCoolants;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.ClientProxy;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static github.kasuminova.ecoaeextension.common.block.ecotech.efabricator.BlockEFabricatorController.*;

@SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized", "unused"})
public class EFabricatorController extends EPartController<EFabricatorPart> {

    public static final int MAX_COOLANT_CACHE = 100_000;
    public static final int WORK_DELAY = 20;

    public static final List<BlockPos> HIDE_POS_LIST = Arrays.asList(
            // Center
            new BlockPos(0, 1, 0),
            new BlockPos(0, -1, 0),

            new BlockPos(0, 1, 1),
            new BlockPos(0, 0, 1),
            new BlockPos(0, -1, 1),

            // Left
            new BlockPos(1, 1, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(1, -1, 0),

            new BlockPos(1, 1, 1),
            new BlockPos(1, 0, 1),
            new BlockPos(1, -1, 1),

            // Right
            new BlockPos(-1, 1, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(-1, -1, 0),

            new BlockPos(-1, 1, 1),
            new BlockPos(-1, 0, 1),
            new BlockPos(-1, -1, 1)
    );

    protected final List<IFluidHandler> coolantInputHandlers = new ArrayList<>();
    protected final List<IFluidHandler> coolantOutputHandlers = new ArrayList<>();

    protected final IItemStorageChannel itemChannel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    protected IItemList<IAEItemStack> outputBuffer = new ItemList();

    protected BlockEFabricatorController parentController = null;
    protected double idleDrain = 64;

    protected EFabricatorMEChannel channel = null;

    protected int length = 0;

    protected int workDelay = WORK_DELAY;
    protected int maxWorkDelay = WORK_DELAY;

    protected int parallelism = 0;
    protected int consumedParallelism = 0;

    protected int coolantCache = 0;

    protected long totalCrafted = 0;

    protected boolean speedupApplied = false;
    protected boolean overclocked = false;
    protected boolean activeCooling = false;

    protected PktEFabricatorGUIData guiDataPacket = null;
    protected volatile boolean guiDataDirty = false;

    public EFabricatorController(final ResourceLocation machineRegistryName) {
        this();
        this.parentMachine = MachineRegistry.getRegistry().getMachine(machineRegistryName);
        this.parentController = BlockEFabricatorController.REGISTRY.get(new ResourceLocation(ECOAEExtension.MOD_ID, machineRegistryName.getPath()));
    }

    public EFabricatorController() {
        this.workMode = WorkMode.SEMI_SYNC;
    }

    protected boolean onSyncTick() {
        if (channel == null || !channel.getProxy().isActive()) {
            this.tickExecutor = null;
            return false;
        }

        workDelay--;
        if (workDelay > 0) {
            this.tickExecutor = null;
            return false;
        }
        workDelay = maxWorkDelay;
        speedupApplied = false;
        clearOutputBuffer();
        supplyWorkerPower();
        supplyCoolantCache();
        return true;
    }

    protected void onAsyncTick() {
        updateGUIDataPacket();

        long prevTotalCrafted = totalCrafted;
        List<EFabricatorWorker> workers = getWorkers();
        workers.forEach(worker -> worker.updateStatus(false));
        for (EFabricatorWorker worker : workers) {
            if (worker.hasWork()) {
                int worked = worker.doWork();
                totalCrafted += worked;
                consumedParallelism += worked <= 1 ? 0 : worked;
            }
        }
        if (activeCooling && hasWork()) {
            convertOverflowParallelismToWorkDelay(parallelism - consumedParallelism);
        }
        consumedParallelism = 0;

        if (prevTotalCrafted != totalCrafted) {
            markNoUpdateSync();
        }
    }

    protected void supplyCoolantCache() {
        if (coolantCache >= MAX_COOLANT_CACHE) {
            return;
        }

        // 写了一坨大的！
        for (final IFluidHandler inputHandler : coolantInputHandlers) {
            for (final IFluidTankProperties property : inputHandler.getTankProperties()) {
                FluidStack contents = property.getContents();
                if (contents == null || contents.amount == 0) {
                    continue;
                }

                MachineCoolants.Coolant coolant = MachineCoolants.INSTANCE.getCoolant(contents.getFluid());
                if (coolant == null) {
                    continue;
                }

                for (final IFluidHandler outputHandler : coolantOutputHandlers) {
                    int maxCanConsume = coolant.maxCanConsume(inputHandler, outputHandler);
                    if (maxCanConsume <= 0) {
                        continue;
                    }

                    int required = MAX_COOLANT_CACHE - coolantCache;
                    int mul = required / coolant.coolantUnit();
                    if (mul * coolant.coolantUnit() < required) {
                        mul++;
                    }
                    mul = Math.min(mul, maxCanConsume);

                    if (mul > 0) {
                        FluidStack input = coolant.input();
                        inputHandler.drain(new FluidStack(input, mul * input.amount), true);
                        FluidStack output = coolant.output();
                        if (output != null) {
                            outputHandler.fill(new FluidStack(output, mul * output.amount), true);
                        }
                        coolantCache += mul * coolant.coolantUnit();
                        if (coolantCache >= MAX_COOLANT_CACHE) {
                            return;
                        }
                    }
                }
            }
        }
    }

    protected void supplyWorkerPower() {
        IEnergyGrid energy;
        try {
            energy = channel.getProxy().getEnergy();
        } catch (GridAccessException ignored) {
            return;
        }

        for (EFabricatorWorker worker : getWorkers()) {
            if (worker.getEnergyCache() < worker.getMaxEnergyCache()) {
                worker.supplyEnergy(
                        (int) energy.extractAEPower(worker.getMaxEnergyCache() - worker.getEnergyCache(), Actionable.MODULATE, PowerMultiplier.CONFIG));
            }
        }
    }

    protected void clearOutputBuffer() {
        try {
            AENetworkProxy proxy = this.channel.getProxy();
            IMEMonitor<IAEItemStack> inv = proxy.getStorage().getInventory(itemChannel);
            for (final IAEItemStack stack : outputBuffer) {
                IAEItemStack notInserted = Platform.poweredInsert(proxy.getEnergy(), inv, stack.copy(), this.channel.getSource());
                if (notInserted != null) {
                    stack.setStackSize(notInserted.getStackSize());
                } else {
                    stack.setStackSize(0);
                }
            }
        } catch (GridAccessException ignored) {
        }
    }

    @Override
    protected void updateComponents() {
        super.updateComponents();
        IDynamicPatternInfo workers = getDynamicPattern("workers");
        this.length = workers != null ? workers.getSize() : 0;
        this.foundComponents.values().forEach(component -> {
            if (component.providedComponent() instanceof IFluidHandler handler) {
                switch (component.getComponent().ioType) {
                    case INPUT -> coolantInputHandlers.add(handler);
                    case OUTPUT -> coolantOutputHandlers.add(handler);
                }
            }
        });
        updateParallelism();
        updateWorkDelay();
    }

    @Override
    protected void onAddPart(final EFabricatorPart part) {
        if (part instanceof EFabricatorMEChannel channel) {
            this.channel = channel;
        }
    }

    protected void clearParts() {
        super.clearParts();
        this.coolantInputHandlers.clear();
        this.coolantOutputHandlers.clear();
        this.channel = null;
        this.length = 0;
    }

    protected void updateParallelism() {
        final double[] parallelism = {0};
        Map<EFabricatorParallelProc.Type, List<Modifier>> modifierMap = getParallelProcs().stream()
                .flatMap(proc -> overclocked ? Stream.concat(proc.modifiers.stream(), proc.overclockModifiers.stream()) : proc.modifiers.stream()) // 超频额外添加超频修正器
                .filter(modifier -> modifier.isBuff() || !activeCooling) // 主动冷却移除超频的负面效果。
                .collect(Collectors.groupingBy(
                        Modifier::getType,
                        () -> new TreeMap<>(Comparator.comparingInt(EFabricatorParallelProc.Type::getPriority)),
                        Collectors.toList())
                );

        modifierMap.values().stream()
                .flatMap(Collection::stream)
                .filter(Modifier::isBuff)
                .forEach(modifier -> parallelism[0] = modifier.apply(parallelism[0]));
        modifierMap.values().stream()
                .flatMap(Collection::stream)
                .filter(Modifier::isDebuff)
                .forEach(modifier -> parallelism[0] = modifier.apply(parallelism[0]));

        this.parallelism = (int) Math.round(parallelism[0]);
    }

    public synchronized void convertOverflowParallelismToWorkDelay(final int overflow) {
        if (overflow <= 0 || speedupApplied) {
            return;
        }
        float ratio = (float) parallelism / overflow;
        int speedUp = Math.min(Math.round(ratio / 0.05f), maxWorkDelay - 1);

        double coolantUsage = parallelism * 0.04;
        int maxCanConsume = (int) (coolantCache / coolantUsage);
        speedUp = Math.min(speedUp, maxCanConsume);
        coolantCache -= (int) Math.round(speedUp * coolantUsage);

        this.workDelay = maxWorkDelay - speedUp;
        this.speedupApplied = true;
    }

    public void updateWorkDelay() {
        if (activeCooling) {
            this.maxWorkDelay = WORK_DELAY - this.getWorkers().size();
        } else {
            this.maxWorkDelay = WORK_DELAY;
        }
    }

    public void recalculateEnergyUsage() {
        double newIdleDrain = 64;
        final int allPatterns = this.getPatternBuses().stream().mapToInt(EFabricatorPatternBus::getValidPatterns).sum();
        newIdleDrain += allPatterns;
        if (this.idleDrain != newIdleDrain) {
            this.idleDrain = newIdleDrain;
            if (this.channel != null) {
                this.channel.getProxy().setIdlePowerUsage(idleDrain);
            }
        }
    }

    public boolean insertPattern(final ItemStack patternStack) {
        for (final EFabricatorPatternBus patternBus : getPatternBuses()) {
            AppEngInternalInventory patternInv = patternBus.getPatterns();
            for (int i = 0; i < patternInv.getSlots(); i++) {
                if (patternInv.getStackInSlot(i).isEmpty()) {
                    patternInv.setStackInSlot(i, ItemUtils.copyStackWithSize(patternStack, 1));
                    return true;
                }
            }
        }

        return false;
    }

    public boolean offerWork(EFabricatorWorker.CraftWork work) {
        boolean success = false;
        for (EFabricatorWorker fabricatorWorker : getWorkers()) {
            if (!fabricatorWorker.isFull()) {
                fabricatorWorker.offerWork(work);
                success = true;
                break;
            }
        }
        if (success && activeCooling && !speedupApplied) {
            convertOverflowParallelismToWorkDelay(parallelism);
        }
        return success;
    }

    public boolean isQueueFull() {
        for (final EFabricatorWorker worker : getWorkers()) {
            if (!worker.isFull()) {
                return false;
            }
        }
        return true;
    }

    public boolean hasWork() {
        for (EFabricatorWorker eFabricatorWorker : getWorkers()) {
            if (eFabricatorWorker.hasWork()) {
                return true;
            }
        }
        return false;
    }

    public synchronized void updateGUIDataPacket() {
        guiDataDirty = true;
    }

    public PktEFabricatorGUIData getGuiDataPacket() {
        if (guiDataDirty || guiDataPacket == null) {
            this.guiDataPacket = new PktEFabricatorGUIData(this);
            this.guiDataDirty = false;
        }
        return guiDataPacket;
    }

    public long getTotalCrafted() {
        return totalCrafted;
    }

    public double getEnergyConsumePerTick() {
        return idleDrain;
    }

    public int getLength() {
        return length;
    }

    public Levels getLevel() {
        if (parentController == L4) {
            return Levels.L4;
        }
        if (parentController == L6) {
            return Levels.L6;
        }
        if (parentController == L9) {
            return Levels.L9;
        }
        ECOAEExtension.log.warn("Invalid EFabricator controller level: {}", parentController);
        return Levels.L4;
    }

    public EFabricatorMEChannel getChannel() {
        return channel;
    }

    public List<EFabricatorWorker> getWorkers() {
        return parts.getParts(EFabricatorWorker.class);
    }

    public List<EFabricatorPatternBus> getPatternBuses() {
        return parts.getParts(EFabricatorPatternBus.class);
    }

    public List<EFabricatorParallelProc> getParallelProcs() {
        return parts.getParts(EFabricatorParallelProc.class);
    }

    public int getParallelism() {
        return parallelism;
    }

    public int getAvailableParallelism() {
        return Math.max(0, parallelism - consumedParallelism);
    }

    public IItemList<IAEItemStack> getOutputBuffer() {
        return outputBuffer;
    }

    public boolean isOverclocked() {
        return overclocked;
    }

    public EFabricatorController setOverclocked(final boolean overclocked) {
        this.overclocked = overclocked;
        updateParallelism();
        updateGUIDataPacket();
        return this;
    }

    public boolean isActiveCooling() {
        return activeCooling;
    }

    public EFabricatorController setActiveCooling(final boolean activeCooling) {
        this.activeCooling = activeCooling;
        updateParallelism();
        updateWorkDelay();
        updateGUIDataPacket();
        return this;
    }

    public int getEnergyStored() {
        return getWorkers().stream().mapToInt(EFabricatorWorker::getEnergyCache).sum();
    }

    public int getCoolantCache() {
        return coolantCache;
    }

    public void consumeCoolant(final int amount) {
        coolantCache -= amount;
    }

    public int getCoolantInputCap() {
        int total = 0;
        for (final IFluidHandler handler : coolantInputHandlers) {
            for (final IFluidTankProperties property : handler.getTankProperties()) {
                total += Math.min(property.getCapacity(), Integer.MAX_VALUE - total);
                if (total == Integer.MAX_VALUE) {
                    return Integer.MAX_VALUE;
                }
            }
        }
        return total;
    }

    public int getCoolantInputFluids() {
        int total = 0;
        for (final IFluidHandler handler : coolantInputHandlers) {
            for (final IFluidTankProperties property : handler.getTankProperties()) {
                FluidStack contents = property.getContents();
                if (contents == null || contents.amount == 0) {
                    continue;
                }
                if (MachineCoolants.INSTANCE.getCoolant(contents.getFluid()) != null) {
                    total += Math.min(contents.amount, Integer.MAX_VALUE - total);
                    if (total == Integer.MAX_VALUE) {
                        return Integer.MAX_VALUE;
                    }
                }
            }
        }
        return total;
    }

    public int getCoolantOutputCap() {
        int total = 0;
        for (final IFluidHandler handler : coolantOutputHandlers) {
            for (final IFluidTankProperties property : handler.getTankProperties()) {
                total += Math.min(property.getCapacity(), Integer.MAX_VALUE - total);
                if (total == Integer.MAX_VALUE) {
                    return Integer.MAX_VALUE;
                }
            }
        }
        return total;
    }

    public int getCoolantOutputFluids() {
        int total = 0;
        for (final IFluidHandler handler : coolantOutputHandlers) {
            for (final IFluidTankProperties property : handler.getTankProperties()) {
                FluidStack contents = property.getContents();
                if (contents == null || contents.amount == 0) {
                    continue;
                }
                total += Math.min(contents.amount, Integer.MAX_VALUE - total);
                if (total == Integer.MAX_VALUE) {
                    return Integer.MAX_VALUE;
                }
            }
        }
        return total;
    }

    public BlockEFabricatorController getParentController() {
        return parentController;
    }

    @Override
    protected Class<? extends Block> getControllerBlock() {
        return BlockEFabricatorController.class;
    }

    @Override
    public void validate() {
        if (!FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }

        ClientProxy.clientScheduler.addRunnable(() -> {
            BlockModelHider.hideOrShowBlocks(HIDE_POS_LIST, this);
            notifyStructureFormedState(isStructureFormed());
        }, 0);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            BlockModelHider.hideOrShowBlocks(HIDE_POS_LIST, this);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }
        ClientProxy.clientScheduler.addRunnable(() -> {
            BlockModelHider.hideOrShowBlocks(HIDE_POS_LIST, this);
            notifyStructureFormedState(isStructureFormed());
        }, 0);
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        boolean prevLoaded = loaded;
        loaded = false;

        super.readCustomNBT(compound);
        totalCrafted = compound.getLong("totalCrafted");
        overclocked = compound.getBoolean("overclock");
        activeCooling = compound.getBoolean("activeCooling");
        coolantCache = compound.getInteger("coolantCache");

        outputBuffer = new ItemList();
        NBTTagList list = compound.getTagList("outputBuffer", Constants.NBT.TAG_COMPOUND);
        IntStream.range(0, list.tagCount())
                .mapToObj(i -> itemChannel.createStack(ItemStackUtils.readNBTOversize(list.getCompoundTagAt(i))))
                .forEach(outputBuffer::add);

        loaded = prevLoaded;

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            ClientProxy.clientScheduler.addRunnable(() -> {
                BlockModelHider.hideOrShowBlocks(HIDE_POS_LIST, this);
                notifyStructureFormedState(isStructureFormed());
            }, 0);
        }
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        compound.setLong("totalCrafted", totalCrafted);
        compound.setBoolean("overclock", overclocked);
        compound.setBoolean("activeCooling", activeCooling);
        compound.setInteger("coolantCache", coolantCache);

        NBTTagList list = new NBTTagList();
        synchronized (outputBuffer) {
            for (final IAEItemStack stack : outputBuffer) {
                list.appendTag(ItemStackUtils.writeNBTOversize(stack.getCachedItemStack(stack.getStackSize())));
            }
        }
        compound.setTag("outputBuffer", list);
    }

    @Override
    protected void readMachineNBT(final NBTTagCompound compound) {
        super.readMachineNBT(compound);
        if (compound.hasKey("parentMachine")) {
            ResourceLocation rl = new ResourceLocation(compound.getString("parentMachine"));
            parentMachine = MachineRegistry.getRegistry().getMachine(rl);
            if (parentMachine != null) {
                this.parentController = BlockEFabricatorController.REGISTRY.get(new ResourceLocation(ECOAEExtension.MOD_ID, parentMachine.getRegistryName().getPath()));
            } else {
                ModularMachinery.log.info("Couldn't find machine named " + rl + " for controller at " + getPos());
            }
        }
    }

}
