package github.kasuminova.ecoaeextension.common.network;

import github.kasuminova.ecoaeextension.client.gui.GuiECalculatorController;
import github.kasuminova.ecoaeextension.common.container.data.ECalculatorData;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorController;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PktECalculatorGUIData implements IMessage, IMessageHandler<PktECalculatorGUIData, IMessage> {

    private ECalculatorData data = null;

    public PktECalculatorGUIData() {
    }

    public PktECalculatorGUIData(final ECalculatorController controller) {
        this.data = ECalculatorData.from(controller);
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        data = ECalculatorData.read(buf);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        data.write(buf);
    }

    @Override
    public IMessage onMessage(final PktECalculatorGUIData message, final MessageContext ctx) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            Minecraft.getMinecraft().addScheduledTask(() -> processPacket(message));
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    protected static void processPacket(final PktECalculatorGUIData message) {
        ECalculatorData data = message.data;
        GuiScreen cur = Minecraft.getMinecraft().currentScreen;
        if (!(cur instanceof GuiECalculatorController ecGUI)) {
            return;
        }
        if (data == null) {
            return;
        }
        ecGUI.onDataUpdate(data);
    }

}
