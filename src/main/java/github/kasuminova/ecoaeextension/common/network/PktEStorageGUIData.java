package github.kasuminova.ecoaeextension.common.network;

import github.kasuminova.ecoaeextension.client.gui.GuiEStorageController;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageLevel;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageType;
import github.kasuminova.ecoaeextension.common.container.data.EStorageCellData;
import github.kasuminova.ecoaeextension.common.container.data.EStorageEnergyData;
import github.kasuminova.ecoaeextension.common.item.estorage.EStorageCell;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageCellDrive;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageController;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PktEStorageGUIData implements IMessage, IMessageHandler<PktEStorageGUIData, IMessage> {

    protected final List<EStorageCellData> dataList = new ArrayList<>();
    protected EStorageEnergyData energyData = null;

    public PktEStorageGUIData() {
    }

    public PktEStorageGUIData(final EStorageController controller) {
        List<EStorageCellDrive> drives = controller.getCellDrives();
        drives.stream()
                .filter(drive -> drive.getDriveInv().getStackInSlot(0).getItem() instanceof EStorageCell<?> cell && drive.isCellSupported(cell.getLevel()))
                .map(EStorageCellData::from)
                .filter(Objects::nonNull)
                .forEach(dataList::add);
        energyData = new EStorageEnergyData(controller.getEnergyStored(), controller.getMaxEnergyStore(), controller.getEnergyConsumePerTick());
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            int type = buf.readByte();
            int level = buf.readByte();
            int usedTypes = buf.readShort();
            long usedBytes = buf.readLong();
            dataList.add(new EStorageCellData(DriveStorageType.values()[type], DriveStorageLevel.values()[level], usedTypes, usedBytes));
        }
        energyData = new EStorageEnergyData(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(dataList.size());
        dataList.forEach(data -> {
            int type = data.type().ordinal();
            int level = data.level().ordinal();
            int usedTypes = data.usedTypes();
            long usedBytes = data.usedBytes();
            buf.writeByte(type);
            buf.writeByte(level);
            buf.writeShort(usedTypes);
            buf.writeLong(usedBytes);
        });
        buf.writeDouble(energyData.energyStored());
        buf.writeDouble(energyData.maxEnergyStore());
        buf.writeDouble(energyData.energyConsumePerTick());
    }

    @Override
    public IMessage onMessage(final PktEStorageGUIData message, final MessageContext ctx) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            processPacket(message);
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    protected static void processPacket(final PktEStorageGUIData message) {
        List<EStorageCellData> dataList = message.dataList;
        EStorageEnergyData energyData = message.energyData;
        GuiScreen cur = Minecraft.getMinecraft().currentScreen;
        if (!(cur instanceof GuiEStorageController)) {
            return;
        }
        List<EStorageCellData> sorted = dataList.stream()
                .sorted((o1, o2) -> {
                    int byteResult = Long.compare(o2.usedBytes(), o1.usedBytes());
                    if (byteResult != 0) {
                        return byteResult;
                    }
                    int typeResult = Integer.compare(o2.usedTypes(), o1.usedTypes());
                    if (typeResult != 0) {
                        return typeResult;
                    }
                    return Integer.compare(o2.level().ordinal(), o1.level().ordinal());
                })
                .collect(Collectors.toList());

        Minecraft.getMinecraft().addScheduledTask(() -> {
            GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
            if (!(currentScreen instanceof GuiEStorageController controllerGUI)) {
                return;
            }
            controllerGUI.setCellDataList(sorted);
            controllerGUI.setEnergyData(energyData);
            controllerGUI.onDataReceived();
        });
    }

}
