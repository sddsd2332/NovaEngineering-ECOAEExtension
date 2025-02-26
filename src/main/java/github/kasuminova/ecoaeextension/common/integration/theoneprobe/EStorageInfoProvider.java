package github.kasuminova.ecoaeextension.common.integration.theoneprobe;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageLevel;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageType;
import github.kasuminova.ecoaeextension.common.container.data.EStorageCellData;
import github.kasuminova.ecoaeextension.common.crafttweaker.util.NovaEngUtils;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageCellDrive;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageEnergyCell;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class EStorageInfoProvider implements IProbeInfoProvider {

    public static final EStorageInfoProvider INSTANCE = new EStorageInfoProvider();

    private EStorageInfoProvider() {
    }

    @Override
    public String getID() {
        return ECOAEExtension.MOD_ID + ':' + "estorage_info_provider";
    }

    @Override
    public void addProbeInfo(final ProbeMode probeMode,
                             final IProbeInfo probeInfo,
                             final EntityPlayer player,
                             final World world,
                             final IBlockState blockState,
                             final IProbeHitData hitData) {
        TileEntity te = world.getTileEntity(hitData.getPos());
        if (te instanceof final EStorageEnergyCell cell) {
            processEnergyCellInfo(probeInfo, cell);
            return;
        }
        if (te instanceof final EStorageCellDrive drive) {
            processCellDriveInfo(probeInfo, drive);
        }
    }

    private static void processCellDriveInfo(final IProbeInfo probeInfo, final EStorageCellDrive drive) {
        IProbeInfo box = newBox(probeInfo);
        IProbeInfo leftInfo = newVertical(box);
        IProbeInfo rightInfo = newVertical(box);

        EStorageController controller = drive.getController();

        leftInfo.text("{*top.estorage.drive.status*}");
        if (controller == null) {
            rightInfo.text("{*top.estorage.drive.status.offline*}");
        } else {
            rightInfo.text("{*top.estorage.drive.status.online*}");
        }

        EStorageCellData data = EStorageCellData.from(drive);
        if (data == null) {
            return;
        }
        DriveStorageLevel level = data.level();
        if (controller != null) {
            if (!drive.isCellSupported(level)) {
                newBox(probeInfo).text("{*top.estorage.drive.cell.unsupported*}");
            }
        }
        DriveStorageType type = data.type();
        String typeName = "gui.estorage_controller.cell_info." + switch (type) {
            case EMPTY -> "unknown";
            case ITEM -> "item";
            case FLUID -> "fluid";
        };
        String levelName = switch (level) {
            case EMPTY -> "unknown";
            case A -> "L4";
            case B -> "L6";
            case C -> "L9";
        };
        leftInfo.text("{*top.estorage.drive.cell*}");
        rightInfo.text(String.format("{*%s*} (%s)", typeName, levelName));

        long usedBytes = data.usedBytes();
        long maxBytes = EStorageCellDrive.getMaxBytes(data);

        leftInfo.text("{*top.estorage.drive.cell.bytes*}");
        rightInfo.text(TextFormatting.YELLOW + String.valueOf(usedBytes) + TextFormatting.BLUE + " / " + TextFormatting.GOLD + maxBytes);

        int usedTypes = data.usedTypes();
        int maxTypes = EStorageCellDrive.getMaxTypes(data);

        leftInfo.text("{*top.estorage.drive.cell.types*}");
        rightInfo.text(TextFormatting.YELLOW + String.valueOf(usedTypes) + TextFormatting.BLUE + " / " + TextFormatting.GOLD + maxTypes);
    }

    private static void processEnergyCellInfo(final IProbeInfo probeInfo, final EStorageEnergyCell cell) {
        double energyStored = cell.getEnergyStored();
        double maxEnergyStore = cell.getMaxEnergyStore();

        String progressStr = String.format("%sAE / %sAE",
                NovaEngUtils.formatNumber(Math.round(energyStored)),
                NovaEngUtils.formatNumber(Math.round(maxEnergyStore))
        );

        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                .progress(Math.round((energyStored / maxEnergyStore) * 200), 200, probeInfo.defaultProgressStyle()
                        .prefix(progressStr)
                        .filledColor(0xCC42B8FF)
                        .alternateFilledColor(0xCC42BDFF)
                        .borderColor(0xCC97FFFF)
                        .backgroundColor(0xFF000000)
                        .numberFormat(NumberFormat.NONE)
                        .width(120)
                );
    }

    private static IProbeInfo newVertical(final IProbeInfo probeInfo) {
        return probeInfo.vertical(probeInfo.defaultLayoutStyle().spacing(0));
    }

    private static IProbeInfo newBox(final IProbeInfo info) {
        return info.horizontal(info.defaultLayoutStyle().borderColor(0x801E90FF));
    }
}
