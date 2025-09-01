package github.kasuminova.ecoaeextension.common.integration.theoneprobe;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.crafttweaker.util.NovaEngUtils;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorWorker;
import github.kasuminova.ecoaeextension.common.util.ColorUtils;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.awt.*;
import java.util.Deque;

public class EFabricatorInfoProvider implements IProbeInfoProvider {

    public static final EFabricatorInfoProvider INSTANCE = new EFabricatorInfoProvider();

    public static final Color LOW_COLOR  = new Color(0xCC54FF9F);
    public static final Color MID_COLOR  = new Color(0xCCFFFF00);
    public static final Color FULL_COLOR = new Color(0xCCFF4500);

    private EFabricatorInfoProvider() {
    }

    @Override
    public String getID() {
        return ECOAEExtension.MOD_ID + ':' + "efabricator_info_provider";
    }

    @Override
    public void addProbeInfo(final ProbeMode probeMode,
                             final IProbeInfo probeInfo,
                             final EntityPlayer player,
                             final World world,
                             final IBlockState blockState,
                             final IProbeHitData hitData) {
        TileEntity te = world.getTileEntity(hitData.getPos());
        if (te instanceof EFabricatorWorker worker) {
            processWorkerInfo(probeInfo, worker);
        }
    }

    private static void processWorkerInfo(final IProbeInfo probeInfo, final EFabricatorWorker worker) {
        IProbeInfo box = newBox(probeInfo);
        IProbeInfo leftInfo = newVertical(box);
        IProbeInfo rightInfo = newVertical(box);

        EFabricatorController controller = worker.getController();

        // Status
        leftInfo.text("{*top.efabricator.worker.status*}");
        if (controller == null) {
            rightInfo.text("{*top.efabricator.worker.status.offline*}");
        } else {
            rightInfo.text("{*top.efabricator.worker.status.online*}");
        }

        EFabricatorWorker.CraftingQueue queue = worker.getQueue();
        int queueDepth = worker.getQueueDepth();
        float percent = (float) queue.size() / queueDepth;

        int color = ColorUtils.getGradientColor(new Color[]{
                LOW_COLOR, LOW_COLOR, LOW_COLOR, MID_COLOR, MID_COLOR, FULL_COLOR, FULL_COLOR
        }, 0xCC, percent).getRGB();

        // Energy bar
        int energyCache = worker.getEnergyCache();
        int maxEnergyCache = worker.getMaxEnergyCache();

        String progressStr = String.format("%sAE / %sAE",
                NovaEngUtils.formatNumber(energyCache),
                NovaEngUtils.formatNumber(maxEnergyCache)
        );
        box = newBox(probeInfo).vertical();

        box.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                .progress(Math.round(((float) energyCache / maxEnergyCache) * 80), 80, probeInfo.defaultProgressStyle()
                        .prefix(progressStr)
                        .filledColor(0xCC42B8FF)
                        .alternateFilledColor(0xCC42BDFF)
                        .borderColor(0xCC97FFFF)
                        .backgroundColor(0xFF000000)
                        .numberFormat(NumberFormat.NONE)
                        .width(120)
                );

        // Queue bar
        progressStr = String.format("%s / %s", queue.size(), queueDepth);
        IProbeInfo progressLine = box.horizontal(box.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
        progressLine.text("{*top.efabricator.worker.queue_status*} ");
        progressLine.horizontal(box.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                .progress(Math.round(percent * queueDepth), queueDepth, box.defaultProgressStyle()
                        .prefix(progressStr)
                        .filledColor(color)
                        .alternateFilledColor(darkenColor(color, .8))
                        .borderColor(lightenColor(color, .8))
                        .backgroundColor(0xFF000000)
                        .numberFormat(NumberFormat.NONE)
                        .width(queueDepthToBarLength(queueDepth))
                );

        Deque<EFabricatorWorker.CraftWork> workDeque = queue.getQueue();
        if (workDeque.isEmpty()) {
            return;
        }

        // Current crafting
        box = newBox(probeInfo);
        IProbeInfo currentCrafting = box.horizontal(box.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
        currentCrafting.text("{*top.efabricator.worker.current_crafting*}");
        EFabricatorWorker.CraftWork peek = workDeque.peek();
        if (peek == null) {
            return;
        }

        currentCrafting.item(peek.getOutput());

        synchronized (worker) {
            if (workDeque.size() <= 1) {
                return;
            }

            // Enqueued crafting
            box = newBox(probeInfo).vertical().text("{*top.efabricator.worker.enqueued_crafting*}");
            IProbeInfo row = box.horizontal();
            int max = Math.min(workDeque.size() - 1, 32);
            int maxItemsPerRow = 8;
            int rowItems = maxItemsPerRow;
            boolean first = true;
            for (final EFabricatorWorker.CraftWork work : workDeque) {
                // Count
                if (rowItems == 0) {
                    rowItems = maxItemsPerRow;
                    row = box.horizontal();
                }

                // Filter first item
                if (first) {
                    first = false;
                    continue;
                }

                // Info
                row.item(work.getOutput());

                // Count
                rowItems--;
                max--;
                if (max <= 0) {
                    break;
                }
            }
        }
    }

    private static int queueDepthToBarLength(int queueDepth) {
        float mul = 1;
        int len = 0;
        int depth = queueDepth;

        while (depth > 0) {
            int append = (int) (Math.min(depth, 32) * mul);
            if (append < 1) {
                break;
            }
            len += append;
            mul *= 0.8F;
            depth -= append;
        }

        return len;
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
