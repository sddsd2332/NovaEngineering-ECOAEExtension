package github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator;

import appeng.api.util.WorldCoord;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import github.kasuminova.mmce.common.helper.IDynamicPatternInfo;
import github.kasuminova.mmce.common.util.DynamicPattern;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.client.util.BlockModelHider;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.BlockECalculatorController;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.Levels;
import github.kasuminova.ecoaeextension.common.ecalculator.ECPUCluster;
import github.kasuminova.ecoaeextension.common.network.PktECalculatorGUIData;
import github.kasuminova.ecoaeextension.common.tile.ecotech.EPartController;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.ClientProxy;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ECalculatorController extends EPartController<ECalculatorPart> {

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

    public static final List<BlockPos> TAIL_HIDE_POS_LIST = Arrays.asList(
            new BlockPos(0, -1, 1),
            new BlockPos(0, -1, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(0, 1, 1),
            new BlockPos(0, 1, 0)
    );

    protected BlockECalculatorController parentController = null;

    protected ECalculatorMEChannel channel = null;
    protected CraftingCPUCluster virtualCPU = null;

    protected int parallelism = 0;
    protected long totalBytes = 0;

    protected PktECalculatorGUIData guiDataPacket = null;
    protected volatile boolean guiDataDirty = false;

    public ECalculatorController(final ResourceLocation machineRegistryName) {
        this();
        this.parentMachine = MachineRegistry.getRegistry().getMachine(machineRegistryName);
        this.parentController = BlockECalculatorController.REGISTRY.get(new ResourceLocation(ECOAEExtension.MOD_ID, machineRegistryName.getPath()));
    }

    public ECalculatorController() {
        this.workMode = WorkMode.SYNC;
    }

    @Override
    protected boolean onSyncTick() {
        if (this.ticksExisted % 5 == 0) {
            updateGUIDataPacket();
        }
        return false;
    }

    @Override
    protected void updateComponents() {
        super.updateComponents();
        recalculateParallelism();
        recalculateTotalBytes();
        // Create / Update virtual cluster
        createVirtualCPU();
    }

    @Override
    protected void disassemble() {
        super.disassemble();
        this.virtualCPU = null;
        this.parallelism = 0;
        this.totalBytes = 0;
    }

    @Override
    protected void onAddPart(final ECalculatorPart part) {
        if (part instanceof ECalculatorMEChannel) {
            this.channel = (ECalculatorMEChannel) part;
        }
    }

    @Override
    protected void clearParts() {
        super.clearParts();
        this.channel = null;
    }

    @SuppressWarnings("DataFlowIssue")
    protected void recalculateParallelism() {
        this.parallelism = getParallelProcs().stream()
                .mapToInt(ECalculatorParallelProc::getParallelism).sum();

        // Update accelerators
        getThreadCores().forEach(threadCore -> threadCore.getCpus().stream()
                .map(ECPUCluster::from)
                .forEach(ecpuCluster -> ecpuCluster.novaeng_ec$setAccelerators(this.parallelism))
        );
    }

    protected void recalculateTotalBytes() {
        this.totalBytes = getCellDrives().stream()
                .mapToLong(ECalculatorCellDrive::getSuppliedBytes).sum();
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getAvailableBytes() {
        List<ECalculatorThreadCore> threadCores = getThreadCores();
        return totalBytes - threadCores.stream().mapToLong(ECalculatorThreadCore::getUsedStorage).sum();
    }

    public long getUsedBytes() {
        return totalBytes - getAvailableBytes();
    }

    private List<ECalculatorCellDrive> getCellDrives() {
        return parts.getParts(ECalculatorCellDrive.class);
    }

    public List<ECalculatorThreadCore> getThreadCores() {
        return parts.getParts(ECalculatorThreadCore.class);
    }

    private List<ECalculatorParallelProc> getParallelProcs() {
        return parts.getParts(ECalculatorParallelProc.class);
    }

    @SuppressWarnings("DataFlowIssue")
    public void onVirtualCPUSubmitJob(final long usedBytes) {
        List<ECalculatorThreadCore> threadCores = getThreadCores();
        for (final ECalculatorThreadCore threadCore : threadCores) {
            if (threadCore.addCPU(virtualCPU, false)) {
                ECPUCluster ecpuCluster = ECPUCluster.from(this.virtualCPU);
                ecpuCluster.novaeng_ec$setAvailableStorage(usedBytes);
                ecpuCluster.novaeng_ec$setVirtualCPUOwner(null);
                this.virtualCPU = null;
                createVirtualCPU();
                return;
            }
        }
        for (final ECalculatorThreadCore threadCore : threadCores) {
            if (threadCore.addCPU(virtualCPU, true)) {
                ECPUCluster ecpuCluster = ECPUCluster.from(this.virtualCPU);
                final long usedExtraBytes = (long) (usedBytes * 0.1F);
                ecpuCluster.novaeng_ec$setAvailableStorage(usedBytes + usedExtraBytes);
                ecpuCluster.novaeng_ec$setUsedExtraStorage(usedExtraBytes);
                ecpuCluster.novaeng_ec$setVirtualCPUOwner(null);
                this.virtualCPU = null;
                createVirtualCPU();
                return;
            }
        }
        ECOAEExtension.log.warn("Failed to submit virtual cluster to thread core, it may be invalid!");
    }

    public void createVirtualCPU() {
        final long availableBytes = getAvailableBytes();
        if (availableBytes < totalBytes * 0.1F) {
            if (this.virtualCPU != null) {
                this.virtualCPU.destroy();
                this.virtualCPU = null;
            }
            return;
        }

        if (this.virtualCPU != null) {
            ECPUCluster eCluster = ECPUCluster.from(this.virtualCPU);
            eCluster.novaeng_ec$setAvailableStorage(availableBytes);
            eCluster.novaeng_ec$setAccelerators(parallelism);
            return;
        }

        boolean canAddCluster = false;
        for (final ECalculatorThreadCore part : getThreadCores()) {
            if (part.canAddCPU()) {
                canAddCluster = true;
                break;
            }
        }

        if (!canAddCluster) {
            return;
        }

        WorldCoord pos = new WorldCoord(getPos());
        this.virtualCPU = new CraftingCPUCluster(pos, pos);
        ECPUCluster eCluster = ECPUCluster.from(this.virtualCPU);
        eCluster.novaeng_ec$setVirtualCPUOwner(this);
        eCluster.novaeng_ec$setAvailableStorage(availableBytes);
        eCluster.novaeng_ec$setAccelerators(parallelism);

        if (channel != null) {
            channel.postCPUClusterChangeEvent();
        }
    }

    public List<CraftingCPUCluster> getClusterList() {
        final List<CraftingCPUCluster> clusters = new ArrayList<>();
        final List<ECalculatorThreadCore> threadCores = getThreadCores();
        for (ECalculatorThreadCore threadCore : threadCores) {
            threadCore.refreshCPUSource();
            clusters.addAll(threadCore.getCpus());
        }
        if (this.virtualCPU != null) {
            // Refresh machine source.
            ECPUCluster.from(this.virtualCPU).novaeng_ec$setVirtualCPUOwner(this);
            clusters.add(this.virtualCPU);
        }
        return clusters;
    }

    public void onClusterChanged() {
    }

    public int getSharedParallelism() {
        return parallelism;
    }

    public Levels getLevel() {
        if (parentController == BlockECalculatorController.L4) {
            return Levels.L4;
        }
        if (parentController == BlockECalculatorController.L6) {
            return Levels.L6;
        }
        if (parentController == BlockECalculatorController.L9) {
            return Levels.L9;
        }
        ECOAEExtension.log.warn("Invalid ECalculator controller level: {}", parentController);
        return Levels.L4;
    }

    public ECalculatorMEChannel getChannel() {
        return channel;
    }

    public synchronized void updateGUIDataPacket() {
        guiDataDirty = true;
    }

    public PktECalculatorGUIData getGuiDataPacket() {
        if (guiDataDirty || guiDataPacket == null) {
            this.guiDataPacket = new PktECalculatorGUIData(this);
            this.guiDataDirty = false;
        }
        return guiDataPacket;
    }

    @Override
    public void validate() {
        if (!FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }

        ClientProxy.clientScheduler.addRunnable(() -> {
            List<BlockPos> posList = new ArrayList<>(HIDE_POS_LIST);
            processDynamicPatternHidePos(posList);
            BlockModelHider.hideOrShowBlocks(posList, this);
            notifyStructureFormedState(isStructureFormed());
        }, 0);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            List<BlockPos> posList = new ArrayList<>(HIDE_POS_LIST);
            processDynamicPatternHidePos(posList);
            BlockModelHider.hideOrShowBlocks(posList, this);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }
        ClientProxy.clientScheduler.addRunnable(() -> {
            List<BlockPos> posList = new ArrayList<>(HIDE_POS_LIST);
            processDynamicPatternHidePos(posList);
            BlockModelHider.hideOrShowBlocks(posList, this);
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
                List<BlockPos> posList = new ArrayList<>(HIDE_POS_LIST);
                processDynamicPatternHidePos(posList);
                BlockModelHider.hideOrShowBlocks(posList, this);
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
                this.parentController = BlockECalculatorController.REGISTRY.get(new ResourceLocation(ECOAEExtension.MOD_ID, parentMachine.getRegistryName().getPath()));
            } else {
                ModularMachinery.log.info("Couldn't find machine named " + rl + " for controller at " + getPos());
            }
        }
    }

    private void processDynamicPatternHidePos(final List<BlockPos> posList) {
        IDynamicPatternInfo workers = getDynamicPattern("workers");
        if (workers != null) {
            int size = workers.getSize();
            DynamicPattern pattern = workers.getPattern();
            BlockPos offset = pattern.getStructureSizeOffset();
            offset = new BlockPos(offset.getX() * size, offset.getY() * size, offset.getZ() * size);
            offset = offset.add(pattern.getStructureSizeOffsetStart());

            for (final BlockPos tailHidePos : TAIL_HIDE_POS_LIST) {
                posList.add(offset.add(tailHidePos));
            }
        }
    }

    public BlockECalculatorController getParentController() {
        return parentController;
    }

    @Override
    protected Class<? extends Block> getControllerBlock() {
        return BlockECalculatorController.class;
    }

}
