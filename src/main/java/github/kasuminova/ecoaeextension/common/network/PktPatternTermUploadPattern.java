package github.kasuminova.ecoaeextension.common.network;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.security.IActionHost;
import appeng.container.implementations.ContainerPatternEncoder;
import appeng.container.slot.SlotRestrictedInput;
import appeng.me.GridAccessException;
import appeng.parts.reporting.AbstractPartEncoder;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorMEChannel;
import github.kasuminova.ecoaeextension.mixin.ae2.AccessorContainerPatternEncoder;
import hellfirepvp.modularmachinery.ModularMachinery;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PktPatternTermUploadPattern implements IMessage, IMessageHandler<PktPatternTermUploadPattern, IMessage> {

    @Override
    public void fromBytes(final ByteBuf buf) {
    }

    @Override
    public void toBytes(final ByteBuf buf) {
    }

    @Override
    public IMessage onMessage(final PktPatternTermUploadPattern message, final MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        ModularMachinery.EXECUTE_MANAGER.addSyncTask(() -> {
            Container container = player.openContainer;
            if (!(container instanceof ContainerPatternEncoder encoder)) {
                return;
            }

            SlotRestrictedInput patternSlotOUT = ((AccessorContainerPatternEncoder) encoder).getPatternSlotOUT();
            ItemStack patternStack = patternSlotOUT.getStack();
            if (patternStack.isEmpty()) {
                return;
            }

            try {
                AbstractPartEncoder part = encoder.getPart();
                IGuiItemObject itemObject = ((AccessorContainerPatternEncoder) encoder).getIGuiItemObject();
                IMachineSet channelNodes;
                if (part != null) {
                    channelNodes = part.getProxy().getGrid().getMachines(EFabricatorMEChannel.class);
                } else if (itemObject instanceof IActionHost wirelessTerm) {
                    channelNodes = wirelessTerm.getActionableNode().getGrid().getMachines(EFabricatorMEChannel.class);
                } else {
                    return;
                }
                for (final IGridNode channelNode : channelNodes) {
                    EFabricatorMEChannel channel = (EFabricatorMEChannel) channelNode.getMachine();
                    if (channel.insertPattern(patternStack)) {
                        patternStack.shrink(1);
                        if (patternStack.isEmpty()) {
                            patternSlotOUT.putStack(ItemStack.EMPTY);
                        } else {
                            patternSlotOUT.putStack(patternStack);
                        }
                        break;
                    }
                }
            } catch (GridAccessException ignored) {
            }
        });
        return null;
    }

}
