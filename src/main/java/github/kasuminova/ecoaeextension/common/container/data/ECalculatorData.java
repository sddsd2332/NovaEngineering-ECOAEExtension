package github.kasuminova.ecoaeextension.common.container.data;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.GridAccessException;
import appeng.util.item.AEItemStack;
import com.github.bsideup.jabel.Desugar;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.Levels;
import github.kasuminova.ecoaeextension.common.ecalculator.ECPUCluster;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorMEChannel;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorThreadCore;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Desugar
public record ECalculatorData(long totalStorage, long usedExtraStorage, int accelerators,
                              List<ThreadCoreData> threadCores, List<ECPUData> ecpuList, int cpuUsagePerSecond) {

    public static ECalculatorData from(final ECalculatorController controller) {
        final long totalStorage = controller.getTotalBytes();
        final int accelerators = controller.getSharedParallelism();
        final List<ECalculatorThreadCore> threadCores = controller.getThreadCores();
        final List<ThreadCoreData> dataList = new ArrayList<>();
        for (final ECalculatorThreadCore threadCore : threadCores) {
            final int hyperThreads = (int) threadCore.getCpus().stream()
                    .map(ECPUCluster::from)
                    .filter(ecpuCluster -> ecpuCluster.novaeng_ec$getUsedExtraStorage() > 0)
                    .count();
            dataList.add(new ThreadCoreData(threadCore.getControllerLevel(), threadCore.getCpus().size() - hyperThreads, hyperThreads, threadCore.getMaxThreads(), threadCore.getMaxHyperThreads()));
        }
        final List<ECPUData> ecpuData = getEcpuData(controller);
        final int cpuUsagePerSecond = ecpuData.stream().mapToInt(ECPUData::cpuUsagePerSecond).sum();
        return new ECalculatorData(totalStorage, ecpuData.stream().mapToLong(ECPUData::usedExtraMemory).sum(), accelerators, dataList, ecpuData, cpuUsagePerSecond);
    }

    @Nonnull
    private static List<ECPUData> getEcpuData(final ECalculatorController controller) {
        final List<ECPUData> ecpuData = new ArrayList<>();
        final ECalculatorMEChannel channel = controller.getChannel();
        if (channel == null) {
            return ecpuData;
        }

        try {
            final ICraftingGrid crafting = channel.getProxy().getCrafting();
            final List<ECalculatorThreadCore> threadCores = controller.getThreadCores();
            for (ICraftingCPU cpu : crafting.getCpus()) {
                if (!(cpu instanceof ECPUCluster ecpu)) {
                    continue;
                }

                ECalculatorThreadCore core = ecpu.novaeng_ec$getController();
                if (core == null || !threadCores.contains(core)) {
                    continue;
                }

                ecpuData.add(new ECPUData(
                        cpu.getFinalOutput(), cpu.getAvailableStorage(), ecpu.novaeng_ec$getUsedExtraStorage(),
                        ecpu.novaeng_ec$getParallelismRecorder().usedTimeAvg(),
                        ecpu.novaeng_ec$getTimeRecorder().usedTimeAvg()
                ));
            }
        } catch (GridAccessException ignored) {
        }

        return ecpuData;
    }

    public void write(final ByteBuf buf) {
        buf.writeLong(totalStorage);
        buf.writeLong(usedExtraStorage);
        buf.writeInt(accelerators);
        buf.writeByte(threadCores.size());
        threadCores.forEach(threadCore -> {
            buf.writeByte(threadCore.type.ordinal());
            buf.writeByte(threadCore.threads);
            buf.writeByte(threadCore.hyperThreads);
            buf.writeByte(threadCore.maxThreads);
            buf.writeByte(threadCore.maxHyperThreads);
        });
        buf.writeByte(ecpuList.size());
        ecpuList.forEach(ecpu -> {
            try {
                ecpu.crafting.writeToPacket(buf);
            } catch (IOException ignored) {
            }
            buf.writeLong(ecpu.usedMemory);
            buf.writeLong(ecpu.usedExtraMemory);
            buf.writeInt(ecpu.parallelismPreSecond);
            buf.writeInt(ecpu.cpuUsagePerSecond);
        });
        buf.writeInt(cpuUsagePerSecond);
    }

    public static ECalculatorData read(final ByteBuf buf) {
        long totalStorage = buf.readLong();
        long usedExtraStorage = buf.readLong();
        int accelerators = buf.readInt();
        byte threadCoreSize = buf.readByte();
        List<ThreadCoreData> threadCores = new ArrayList<>();
        if (threadCoreSize > 0) {
            for (byte i = 0; i < threadCoreSize; i++) {
                Levels type = Levels.values()[buf.readByte()];
                int threads = buf.readByte();
                int hyperThreads = buf.readByte();
                int maxThreads = buf.readByte();
                int maxHyperThreads = buf.readByte();
                threadCores.add(new ThreadCoreData(type, threads, hyperThreads, maxThreads, maxHyperThreads));
            }
        }
        byte ecpuSize = buf.readByte();
        List<ECPUData> ecpuList = new ArrayList<>();
        if (ecpuSize > 0) {
            for (byte i = 0; i < ecpuSize; i++) {
                ecpuList.add(new ECPUData(AEItemStack.fromPacket(buf), buf.readLong(), buf.readLong(), buf.readInt(), buf.readInt()));
            }
        }
        int cpuUsagePerSecond = buf.readInt();
        return new ECalculatorData(totalStorage, usedExtraStorage, accelerators, threadCores, ecpuList, cpuUsagePerSecond);
    }

    @Desugar
    public record ECPUData(IAEItemStack crafting, long usedMemory, long usedExtraMemory, int parallelismPreSecond, int cpuUsagePerSecond) {
    }

    @Desugar
    public record ThreadCoreData(Levels type, int threads, int hyperThreads, int maxThreads, int maxHyperThreads) {
    }

}
