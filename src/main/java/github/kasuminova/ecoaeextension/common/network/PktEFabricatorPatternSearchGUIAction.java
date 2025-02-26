package github.kasuminova.ecoaeextension.common.network;

import appeng.tile.inventory.AppEngInternalInventory;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.container.ContainerEFabricatorPatternSearch;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorPatternBus;
import hellfirepvp.modularmachinery.ModularMachinery;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PktEFabricatorPatternSearchGUIAction implements IMessage, IMessageHandler<PktEFabricatorPatternSearchGUIAction, IMessage> {

    private Action action = null;
    private BlockPos pos = null;
    private int slot;

    public PktEFabricatorPatternSearchGUIAction() {
    }

    public PktEFabricatorPatternSearchGUIAction(final Action action) {
        this.action = action;
    }

    public PktEFabricatorPatternSearchGUIAction(final Action action, final BlockPos pos, final int slot) {
        this.action = action;
        this.pos = pos;
        this.slot = slot;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        action = Action.values()[buf.readByte()];
        if (action == Action.PICKUP_PATTERN) {
            pos = BlockPos.fromLong(buf.readLong());
            slot = buf.readByte();
        }
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeByte(action.ordinal());
        if (action == Action.PICKUP_PATTERN) {
            buf.writeLong(pos.toLong());
            buf.writeByte(slot);
        }
    }

    @Override
    public IMessage onMessage(final PktEFabricatorPatternSearchGUIAction message, final MessageContext ctx) {
        final EntityPlayerMP player = ctx.getServerHandler().player;
        if (!(player.openContainer instanceof ContainerEFabricatorPatternSearch efGUIContainer)) {
            return null;
        }

        final EFabricatorController owner = efGUIContainer.getOwner();
        ModularMachinery.EXECUTE_MANAGER.addSyncTask(() -> {
            if (message.action == Action.PUT_PATTERN) {
                final ItemStack stackInMouse = player.inventory.getItemStack();
                if (stackInMouse.isEmpty()) {
                    return;
                }

                if (owner.insertPattern(stackInMouse)) {
                    stackInMouse.shrink(1);
                    if (stackInMouse.isEmpty()) {
                        player.inventory.setItemStack(ItemStack.EMPTY);
                        ECOAEExtension.NET_CHANNEL.sendTo(new PktMouseItemUpdate(ItemStack.EMPTY), player);
                    } else {
                        ECOAEExtension.NET_CHANNEL.sendTo(new PktMouseItemUpdate(stackInMouse), player);
                    }
                }

                return;
            }

            final List<EFabricatorPatternBus> patternBuses = owner.getPatternBuses();
            for (final EFabricatorPatternBus patternBus : patternBuses) {
                final BlockPos pos = message.pos;
                if (!patternBus.getPos().equals(pos)) {
                    continue;
                }

                final AppEngInternalInventory patterns = patternBus.getPatterns();
                final int slot = message.slot;
                final ItemStack stackInSlot = patterns.getStackInSlot(slot);

                if (!stackInSlot.isEmpty() && player.inventory.getItemStack().isEmpty()) {
                    ItemStack newItemStack = patterns.extractItem(slot, stackInSlot.getCount(), false);
                    player.inventory.setItemStack(newItemStack);
                    ECOAEExtension.NET_CHANNEL.sendTo(new PktMouseItemUpdate(newItemStack), player);
                }

                break;
            }
        });

        return null;
    }

    public enum Action {
        PICKUP_PATTERN,
        PUT_PATTERN,
    }

}
