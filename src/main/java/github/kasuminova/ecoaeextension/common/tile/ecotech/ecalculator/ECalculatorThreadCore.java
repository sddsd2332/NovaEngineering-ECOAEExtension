package github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator;

import appeng.api.util.WorldCoord;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import github.kasuminova.mmce.common.util.Sides;
import github.kasuminova.ecoaeextension.common.ecalculator.ECPUCluster;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.List;

public class ECalculatorThreadCore extends ECalculatorPart {

    private static final ThreadLocal<Boolean> WRITE_CPU_NBT = ThreadLocal.withInitial(() -> true);

    protected final ObjectArrayList<CraftingCPUCluster> cpus = new ObjectArrayList<>();

    protected int threads = 0;
    protected int maxThreads = 0;
    protected int maxHyperThreads = 0;

    public ECalculatorThreadCore() {
    }

    public ECalculatorThreadCore(final int maxThreads, final int maxHyperThreads) {
        this.maxThreads = maxThreads;
        this.maxHyperThreads = maxHyperThreads;
    }

    public List<CraftingCPUCluster> getCpus() {
        return cpus;
    }

    public boolean addCPU(final CraftingCPUCluster cluster, final boolean hyperThread) {
        if (cpus.size() >= maxThreads) {
            if (!hyperThread || cpus.size() >= maxThreads + maxHyperThreads) {
                return false;
            }
        }

        final boolean prevEmpty = cpus.isEmpty();

        ECPUCluster.from(cluster).novaeng_ec$setThreadCore(this);
        cpus.add(cluster);

        if (prevEmpty) {
            markForUpdateSync();
        }
        return true;
    }

    public boolean canAddCPU() {
        return cpus.size() < (maxThreads + maxHyperThreads);
    }

    /**
     * Client side only.
     */
    public int getThreads() {
        return threads;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getMaxHyperThreads() {
        return maxHyperThreads;
    }

    public void refreshCPUSource() {
        for (final CraftingCPUCluster cluster : cpus) {
            ECPUCluster eCluster = ECPUCluster.from(cluster);
            // Refresh machine source.
            eCluster.novaeng_ec$setThreadCore(this);
        }
    }

    public void onBlockDestroyed() {
        cpus.forEach(cluster -> ECPUCluster.from(cluster).novaeng_ec$markDestroyed());
        cpus.clear();
        ECalculatorController controller = getController();
        if (controller != null && controller.getChannel() != null) {
            controller.onClusterChanged();
        }
    }

    public void onCPUDestroyed(final CraftingCPUCluster cluster) {
        cpus.remove(cluster);
        ECalculatorController controller = getController();
        if (controller != null) {
            controller.onClusterChanged();
        }
        if (cpus.isEmpty()) {
            markForUpdateSync();
        }
    }

    public long getUsedStorage() {
        if (cpus.isEmpty()) {
            return 0L;
        }
        return cpus.stream().mapToLong(CraftingCPUCluster::getAvailableStorage).sum();
    }

    @Nonnull
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        try {
            WRITE_CPU_NBT.set(false);
            return super.getUpdatePacket();
        } finally {
            WRITE_CPU_NBT.set(true);
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        try {
            WRITE_CPU_NBT.set(false);
            return super.getUpdateTag();
        } finally {
            WRITE_CPU_NBT.set(true);
        }
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);

        if (compound.hasKey("maxClusterCount")) {
            this.maxThreads = compound.getByte("maxClusterCount");
        }
        if (compound.hasKey("maxClusterCountHyperThread")) {
            this.maxHyperThreads = compound.getByte("maxClusterCountHyperThread");
        }
        this.threads = compound.getByte("threads");

        readCPUNBT(compound);
        updateContainingBlockInfo();
    }

    public void readCPUNBT(final NBTTagCompound compound) {
        cpus.clone().forEach(CraftingCPUCluster::destroy);
        cpus.clear();

        final NBTTagList clustersTag = compound.getTagList("clusters", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < clustersTag.tagCount(); i++) {
            NBTTagCompound clusterTag = clustersTag.getCompoundTagAt(i);

            WorldCoord coord = new WorldCoord(getPos());
            CraftingCPUCluster cluster = new CraftingCPUCluster(coord, coord);
            ECPUCluster eCluster = ECPUCluster.from(cluster);

            eCluster.novaeng_ec$setThreadCore(this);
            eCluster.novaeng_ec$setAvailableStorage(clusterTag.getLong("availableStorage"));
            eCluster.novaeng_ec$setUsedExtraStorage(clusterTag.getLong("usedExtraStorage"));
            cluster.readFromNBT(clusterTag);
            cpus.add(cluster);
        }
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        compound.setByte("maxClusterCount", (byte) maxThreads);
        compound.setByte("maxClusterCountHyperThread", (byte) maxHyperThreads);
        compound.setByte("threads", (byte) this.cpus.size());

        if (Sides.isRunningOnClient() || (Sides.isRunningOnServer() && WRITE_CPU_NBT.get())) {
            writeCPUNBT(compound);
        }
    }

    public void writeCPUNBT(final NBTTagCompound compound) {
        final NBTTagList clustersTag = new NBTTagList();
        cpus.forEach(cluster -> {
            ECPUCluster eCluster = ECPUCluster.from(cluster);
            NBTTagCompound clusterTag = new NBTTagCompound();
            cluster.writeToNBT(clusterTag);
            clusterTag.setLong("availableStorage", cluster.getAvailableStorage());
            clusterTag.setLong("usedExtraStorage", eCluster.novaeng_ec$getUsedExtraStorage());
            clustersTag.appendTag(clusterTag);
        });
        compound.setTag("clusters", clustersTag);
    }

    @Override
    public void onDisassembled() {
        super.onDisassembled();
        markForUpdateSync();
    }

    @Override
    public void onAssembled() {
        super.onAssembled();
        markForUpdateSync();
    }

}
