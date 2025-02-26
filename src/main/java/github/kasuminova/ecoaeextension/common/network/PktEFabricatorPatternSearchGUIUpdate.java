package github.kasuminova.ecoaeextension.common.network;

import github.kasuminova.ecoaeextension.client.gui.GuiEFabricatorPatternSearch;
import github.kasuminova.ecoaeextension.common.container.data.EFabricatorPatternData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PktEFabricatorPatternSearchGUIUpdate implements IMessage, IMessageHandler<PktEFabricatorPatternSearchGUIUpdate, IMessage> {

    private UpdateType type = null;
    private EFabricatorPatternData data = null;

    public PktEFabricatorPatternSearchGUIUpdate() {
    }

    public PktEFabricatorPatternSearchGUIUpdate(final UpdateType type, final EFabricatorPatternData data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        type = UpdateType.values()[buf.readByte()];
        data = EFabricatorPatternData.readFrom(buf);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeByte(type.ordinal());
        data.writeTo(buf);
    }

    @Override
    public IMessage onMessage(final PktEFabricatorPatternSearchGUIUpdate message, final MessageContext ctx) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            Minecraft.getMinecraft().addScheduledTask(() -> processPacket(message));
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    protected static void processPacket(final PktEFabricatorPatternSearchGUIUpdate message) {
        EFabricatorPatternData data = message.data;
        GuiScreen cur = Minecraft.getMinecraft().currentScreen;
        if (!(cur instanceof GuiEFabricatorPatternSearch efGUI)) {
            return;
        }
        if (data == null) {
            return;
        }
        efGUI.onDataUpdate(data, message.type == UpdateType.FULL);
    }

    public enum UpdateType {
        SINGLE,
        FULL
    }

}
