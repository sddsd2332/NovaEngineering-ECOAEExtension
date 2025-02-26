package github.kasuminova.ecoaeextension.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PktMouseItemUpdate implements IMessage, IMessageHandler<PktMouseItemUpdate, IMessage> {

    private ItemStack stack = null;

    public PktMouseItemUpdate() {
    }

    public PktMouseItemUpdate(final ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, stack);
    }

    @Override
    public IMessage onMessage(final PktMouseItemUpdate message, final MessageContext ctx) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            Minecraft.getMinecraft().addScheduledTask(() -> processPacket(message));
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    private static void processPacket(final PktMouseItemUpdate message) {
        Minecraft.getMinecraft().player.inventory.setItemStack(message.stack);
    }

}
