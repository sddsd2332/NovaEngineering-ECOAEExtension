package github.kasuminova.ecoaeextension.common.integration.theoneprobe;

import appeng.api.storage.data.IAEItemStack;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import github.kasuminova.mmce.common.util.TimeRecorder;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.crafttweaker.util.NovaEngUtils;
import github.kasuminova.ecoaeextension.common.ecalculator.ECPUCluster;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorThreadCore;
import github.kasuminova.ecoaeextension.common.util.ColorUtils;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.awt.*;
import java.util.List;

public class ECalculatorInfoProvider implements IProbeInfoProvider {

    public static final ECalculatorInfoProvider INSTANCE = new ECalculatorInfoProvider();

    public static final Color LOW_COLOR  = new Color(0xCC54FF9F);
    public static final Color MID_COLOR  = new Color(0xCCFFFF00);
    public static final Color FULL_COLOR = new Color(0xCCFF4500);

    private static final int CPU_USAGE_GREEN_THRESHOLD = 400;
    private static final int CPU_USAGE_YELLOW_THRESHOLD = 800;
    private static final int CPU_USAGE_RED_THRESHOLD = 1200;

    private static final int GLOBAL_CPU_USAGE_GREEN_THRESHOLD = 2000;
    private static final int GLOBAL_CPU_USAGE_YELLOW_THRESHOLD = 4000;
    private static final int GLOBAL_CPU_USAGE_RED_THRESHOLD = 6000;

    private ECalculatorInfoProvider() {
    }

    @Override
    public String getID() {
        return ECOAEExtension.MOD_ID + ':' + "ecalculator_info_provider";
    }

    @Override
    public void addProbeInfo(final ProbeMode mode, final IProbeInfo probeInfo, final EntityPlayer player, final World world, final IBlockState blockState, final IProbeHitData data) {
        final TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof final ECalculatorController controller) {
            processControllerInfo(probeInfo, controller);
        } else if (te instanceof final ECalculatorThreadCore threadCore) {
            processThreadCoreInfo(probeInfo, threadCore);
        }
    }

    private static void processThreadCoreInfo(final IProbeInfo probeInfo, final ECalculatorThreadCore threadCore) {
        final boolean online = threadCore.getController() != null;
        IProbeInfo box = newBox(probeInfo);
        box.text("{*top.ecalculator.thread_core.status*}" + 
                 (online ? "{*top.ecalculator.thread_core.status.online*}" : "{*top.ecalculator.thread_core.status.offline*}")
        );
        if (!online) {
            return;
        }

        final List<CraftingCPUCluster> cpus = threadCore.getCpus();
        final int maxThreads = threadCore.getMaxThreads();
        final int maxHyperThreads = threadCore.getMaxHyperThreads();
        float percent = (float) cpus.size() / (maxThreads + maxHyperThreads);
        final int color = ColorUtils.getGradientColor(new Color[]{
                LOW_COLOR, LOW_COLOR, LOW_COLOR, MID_COLOR, MID_COLOR, FULL_COLOR, FULL_COLOR
        }, 0xCC, percent).getRGB();
        final String progressStr = String.format("%s / %s %s(+%s)%s", cpus.size(), maxThreads, TextFormatting.YELLOW, maxHyperThreads, TextFormatting.RESET);
        newBox(probeInfo).horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                .text(TextFormatting.AQUA + "{*top.ecalculator.thread_core.threads*}")
                .progress((int) (percent * 75), 75, probeInfo.defaultProgressStyle()
                        .prefix(progressStr)
                        .filledColor(color)
                        .alternateFilledColor(darkenColor(color, .8))
                        .borderColor(lightenColor(color, .8))
                        .backgroundColor(0xFF000000)
                        .numberFormat(NumberFormat.NONE)
                        .width(75)
                );

        if (cpus.isEmpty()) {
            return;
        }

        box = newBox(probeInfo).vertical();
        box.text("{*top.ecalculator.thread_core.crafting*}");
        for (final CraftingCPUCluster cpu : cpus) {
            final IAEItemStack output = cpu.getFinalOutput();
            if (output == null) {
                continue;
            }

            final IProbeInfo taskBox = newBox(box).vertical();
            IProbeInfo row = taskBox.horizontal(taskBox.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));

            final long count = output.getStackSize();
            final ItemStack stack = output.getCachedItemStack(1);
            row.item(stack);
            row.itemLabel(stack);
            row.text("  x " + count);

            final ECPUCluster ecpu = ECPUCluster.from(cpu);
            int cpuUsagePerTick = ecpu.novaeng_ec$getTimeRecorder().usedTimeAvg();
            int parallelismPerTick = ecpu.novaeng_ec$getParallelismRecorder().usedTimeAvg();
            row = taskBox.horizontal();

            TextFormatting cpuUsageColor;
            if (cpuUsagePerTick < CPU_USAGE_GREEN_THRESHOLD) {
                cpuUsageColor = TextFormatting.GREEN;
            } else if (cpuUsagePerTick < CPU_USAGE_YELLOW_THRESHOLD) {
                cpuUsageColor = TextFormatting.YELLOW;
            } else if (cpuUsagePerTick < CPU_USAGE_RED_THRESHOLD) {
                cpuUsageColor = TextFormatting.RED;
            } else {
                cpuUsageColor = TextFormatting.DARK_RED;
            }

            row.text("{*top.ecalculator.thread_core.avg_time_usage*}" + cpuUsageColor + cpuUsagePerTick + TextFormatting.RESET + "µs/t");
            row.text("{*top.ecalculator.thread_core.avg_parallelism*}" + TextFormatting.DARK_PURPLE + parallelismPerTick + "/t");
        }
    }

    private static void processControllerInfo(final IProbeInfo probeInfo, final ECalculatorController controller) {
        if (!controller.isStructureFormed() || !controller.isAssembled()) {
            return;
        }
        final long totalMemory = controller.getTotalBytes();
        final long usedMemory = controller.getUsedBytes();
        float percent = (float) usedMemory / totalMemory;
        int color = ColorUtils.getGradientColor(new Color[]{
                LOW_COLOR, LOW_COLOR, LOW_COLOR, MID_COLOR, MID_COLOR, FULL_COLOR, FULL_COLOR
        }, 0xCC, percent).getRGB();
        String progressStr = String.format("%s / %s", NovaEngUtils.formatNumber(usedMemory), NovaEngUtils.formatNumber(totalMemory));
        newBox(probeInfo).horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                .text(TextFormatting.AQUA + "{*top.ecalculator.controller.storage*}")
                .progress((int) (percent * 150), 150, probeInfo.defaultProgressStyle()
                        .prefix(progressStr)
                        .filledColor(color)
                        .alternateFilledColor(darkenColor(color, .8))
                        .borderColor(lightenColor(color, .8))
                        .backgroundColor(0xFF000000)
                        .numberFormat(NumberFormat.NONE)
                        .width(150)
                );

        final int totalWorking = controller.getThreadCores().stream().mapToInt(core -> core.getCpus().size()).sum();
        final int maxThreads = controller.getThreadCores().stream().mapToInt(ECalculatorThreadCore::getMaxThreads).sum();
        final int maxHyperThreads = controller.getThreadCores().stream().mapToInt(ECalculatorThreadCore::getMaxHyperThreads).sum();
        percent = (float) totalWorking / (maxThreads + maxHyperThreads);
        color = ColorUtils.getGradientColor(new Color[]{
                LOW_COLOR, LOW_COLOR, LOW_COLOR, MID_COLOR, MID_COLOR, FULL_COLOR, FULL_COLOR
        }, 0xCC, percent).getRGB();
        progressStr = String.format("%s / %s %s(+%s)%s", totalWorking, maxThreads, TextFormatting.YELLOW, maxHyperThreads, TextFormatting.RESET);
        newBox(probeInfo).horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                .text(TextFormatting.AQUA + "{*top.ecalculator.controller.threads*}")
                .progress((int) (percent * 100), 100, probeInfo.defaultProgressStyle()
                        .prefix(progressStr)
                        .filledColor(color)
                        .alternateFilledColor(darkenColor(color, .8))
                        .borderColor(lightenColor(color, .8))
                        .backgroundColor(0xFF000000)
                        .numberFormat(NumberFormat.NONE)
                        .width(100)
                );

        final IProbeInfo box = newBox(probeInfo);
        final IProbeInfo leftInfo = newVertical(box);
        final IProbeInfo rightInfo = newVertical(box);

        final int parallelism = controller.getSharedParallelism();
        leftInfo.text("{*top.ecalculator.controller.parallelism*}");
        rightInfo.text(TextFormatting.DARK_PURPLE + NovaEngUtils.formatDecimal(parallelism));

        final int totalParallelismPerSecond = controller.getThreadCores().stream()
                .flatMap(core -> core.getCpus().stream())
                .map(ECPUCluster::from)
                .map(ECPUCluster::novaeng_ec$getParallelismRecorder)
                .mapToInt(TimeRecorder::usedTimeAvg)
                .sum();

        leftInfo.text("{*top.ecalculator.controller.avg_parallelism*}");
        rightInfo.text(TextFormatting.DARK_PURPLE + NovaEngUtils.formatDecimal(totalParallelismPerSecond) + "/t");

        final int totalCPUUsagePerSecond = controller.getThreadCores().stream()
                .flatMap(core -> core.getCpus().stream())
                .map(ECPUCluster::from)
                .map(ECPUCluster::novaeng_ec$getTimeRecorder)
                .mapToInt(TimeRecorder::usedTimeAvg)
                .sum();

        TextFormatting cpuUsageColor;
        if (totalCPUUsagePerSecond < GLOBAL_CPU_USAGE_GREEN_THRESHOLD) {
            cpuUsageColor = TextFormatting.GREEN;
        } else if (totalCPUUsagePerSecond < GLOBAL_CPU_USAGE_YELLOW_THRESHOLD) {
            cpuUsageColor = TextFormatting.YELLOW;
        } else if (totalCPUUsagePerSecond < GLOBAL_CPU_USAGE_RED_THRESHOLD) {
            cpuUsageColor = TextFormatting.RED;
        } else {
            cpuUsageColor = TextFormatting.DARK_RED;
        }

        leftInfo.text("{*top.ecalculator.controller.avg_time_usage*}");
        rightInfo.text(cpuUsageColor + NovaEngUtils.formatDecimal(totalCPUUsagePerSecond) + "µs/t");
    }

    // Utility methods to darken and lighten colors

    private static int darkenColor(int color, double factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lightenColor(int color, double factor) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) / factor));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) / factor));
        int b = Math.min(255, (int) ((color & 0xFF) / factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static IProbeInfo newVertical(final IProbeInfo probeInfo) {
        return probeInfo.vertical(probeInfo.defaultLayoutStyle().spacing(0));
    }

    private static IProbeInfo newBox(final IProbeInfo info) {
        return info.horizontal(info.defaultLayoutStyle().borderColor(0x801E90FF));
    }

}
