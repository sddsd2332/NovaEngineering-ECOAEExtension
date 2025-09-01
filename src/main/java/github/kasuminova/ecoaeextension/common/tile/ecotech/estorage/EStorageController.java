package github.kasuminova.ecoaeextension.common.tile.ecotech.estorage;

import appeng.api.config.Actionable;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.client.util.BlockModelHider;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.BlockEStorageController;
import github.kasuminova.ecoaeextension.common.estorage.ECellDriveWatcher;
import github.kasuminova.ecoaeextension.common.tile.ecotech.EPartController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.bus.EStorageBus;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.ClientProxy;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.*;

public class EStorageController extends EPartController<EStoragePart> {

    public static final List<BlockPos> HIDE_POS_LIST = Arrays.asList(
            new BlockPos(0, 1, 0),
            new BlockPos(0, -1, 0),

            new BlockPos(1, 1, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(1, -1, 0),

            new BlockPos(0, 1, 1),
            new BlockPos(0, 0, 1),
            new BlockPos(0, -1, 1),

            new BlockPos(1, 1, 1),
            new BlockPos(1, 0, 1),
            new BlockPos(1, -1, 1)
    );

    protected final Queue<EStorageEnergyCell> energyCellsMin = new PriorityQueue<>(Comparator.reverseOrder());
    protected final Queue<EStorageEnergyCell> energyCellsMax = new PriorityQueue<>();

    protected BlockEStorageController parentController = null;
    protected double idleDrain = 64;

    protected EStorageMEChannel channel = null;

    public EStorageController(final ResourceLocation machineRegistryName) {
        this.workMode = WorkMode.SYNC;
        this.parentMachine = MachineRegistry.getRegistry().getMachine(machineRegistryName);
        this.parentController = BlockEStorageController.REGISTRY.get(new ResourceLocation(ECOAEExtension.MOD_ID, machineRegistryName.getPath()));
    }

    public EStorageController() {
        this.workMode = WorkMode.SYNC;
    }

    protected boolean onSyncTick() {
        if (world.getTotalWorldTime() % 5 == 0) {
            getCellDrives().forEach(EStorageCellDrive::updateWriteState);
            this.energyCellsMax.forEach(cell -> {
                if (cell.shouldRecalculateCap()) {
                    cell.recalculateCapacity();
                }
            });
        }
        return false;
    }

    @Override
    protected void onAddPart(final EStoragePart part) {
        if (part instanceof EStorageEnergyCell energyCell) {
            energyCellsMax.add(energyCell);
            energyCellsMin.add(energyCell);
        } else if (part instanceof EStorageMEChannel c) {
            this.channel = c;
        }
    }

    protected void clearParts() {
        super.clearParts();
        this.energyCellsMax.clear();
        this.energyCellsMin.clear();
        this.channel = null;
    }

    public double injectPower(final double amt, final Actionable mode) {
        double toInject = amt;

        if (mode == Actionable.SIMULATE) {
            for (final EStorageEnergyCell cell : energyCellsMin) {
                double prev = toInject;
                toInject -= (toInject - cell.injectPower(toInject, mode));
                if (toInject <= 0 || prev == toInject) {
                    break;
                }
            }
            return toInject;
        }

        List<EStorageEnergyCell> toReInsert = new LinkedList<>();
        EStorageEnergyCell cell;
        while ((cell = energyCellsMin.poll()) != null) {
            double prev = toInject;
            toInject -= (toInject - cell.injectPower(toInject, mode));
            toReInsert.add(cell);
            if (toInject <= 0 || prev < toInject) {
                break;
            }
        }

        if (!toReInsert.isEmpty()) {
            energyCellsMin.addAll(toReInsert);
        }

        return toInject;
    }

    public double extractPower(final double amt, final Actionable mode) {
        double extracted = 0;

        if (mode == Actionable.SIMULATE) {
            for (final EStorageEnergyCell cell : energyCellsMax) {
                double prev = extracted;
                extracted += cell.extractPower(amt - extracted, mode);
                if (extracted >= amt || prev >= extracted) {
                    break;
                }
            }
            return extracted;
        }

        EStorageEnergyCell cell;
        List<EStorageEnergyCell> toReInsert = new LinkedList<>();
        while ((cell = energyCellsMax.poll()) != null) {
            double prev = extracted;
            extracted += cell.extractPower(amt - extracted, mode);
            toReInsert.add(cell);
            if (extracted >= amt || prev == extracted) {
                break;
            }
        }

        if (!toReInsert.isEmpty()) {
            energyCellsMax.addAll(toReInsert);
        }

        return extracted;
    }

    public void recalculateEnergyUsage() {
        double newIdleDrain = 64;
        for (final EStorageCellDrive drive : getCellDrives()) {
            ECellDriveWatcher<IAEItemStack> watcher = drive.getWatcher();
            if (watcher == null) {
                continue;
            }
            ICellInventoryHandler<?> cellInventory = (ICellInventoryHandler<?>) watcher.getInternal();
            if (cellInventory == null) {
                continue;
            }
            ICellInventory<?> cellInv = cellInventory.getCellInv();
            if (cellInv == null) {
                continue;
            }
            newIdleDrain += cellInv.getIdleDrain();
        }
        this.idleDrain = newIdleDrain;
        if (this.channel != null) {
            this.channel.getProxy().setIdlePowerUsage(idleDrain);
        }
    }

    @Override
    protected Class<? extends Block> getControllerBlock() {
        return BlockEStorageController.class;
    }

    public double getEnergyStored() {
        double energyStored = 0;
        for (final EStorageEnergyCell cell : energyCellsMax) {
            double stored = cell.getEnergyStored();
            if (stored <= 0.000001) {
                break;
            }
            energyStored += stored;
        }
        return energyStored;
    }

    public double getMaxEnergyStore() {
        double maxEnergyStore = 0;
        for (final EStorageEnergyCell energyCell : energyCellsMax) {
            maxEnergyStore += energyCell.getMaxEnergyStore();
        }
        return maxEnergyStore;
    }

    @Override
    public void validate() {
        super.validate();
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

        loaded = prevLoaded;

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            ClientProxy.clientScheduler.addRunnable(() -> {
                BlockModelHider.hideOrShowBlocks(HIDE_POS_LIST, this);
                notifyStructureFormedState(isStructureFormed());
            }, 0);
        }
    }

    @Override
    protected void readMachineNBT(final NBTTagCompound compound) {
        super.readMachineNBT(compound);
        if (compound.hasKey("parentMachine")) {
            ResourceLocation rl = new ResourceLocation(compound.getString("parentMachine"));
            parentMachine = MachineRegistry.getRegistry().getMachine(rl);
            if (parentMachine != null) {
                this.parentController = BlockEStorageController.REGISTRY.get(new ResourceLocation(ECOAEExtension.MOD_ID, parentMachine.getRegistryName().getPath()));
            } else {
                ModularMachinery.log.info("Couldn't find machine named " + rl + " for controller at " + getPos());
            }
        }
    }

    public double getEnergyConsumePerTick() {
        return idleDrain;
    }

    public List<EStorageBus> getStorageBuses() {
        return this.parts.getParts(EStorageBus.class);
    }

    public List<EStorageCellDrive> getCellDrives() {
        return this.parts.getParts(EStorageCellDrive.class);
    }

    @Nullable
    public EStorageMEChannel getChannel() {
        return channel;
    }

    public BlockEStorageController getParentController() {
        return parentController;
    }

}
