package github.kasuminova.ecoaeextension.common.network;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.CommonProxy;
import github.kasuminova.ecoaeextension.common.container.ContainerEFabricatorController;
import github.kasuminova.ecoaeextension.common.container.ContainerEFabricatorPatternSearch;
import github.kasuminova.ecoaeextension.common.container.data.EFabricatorPatternData;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorController;
import hellfirepvp.modularmachinery.ModularMachinery;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PktEFabricatorGUIAction implements IMessage, IMessageHandler<PktEFabricatorGUIAction, IMessage> {

    private Action action = null;

    public PktEFabricatorGUIAction() {
    }

    public PktEFabricatorGUIAction(final Action action) {
        this.action = action;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        this.action = Action.values()[buf.readByte()];
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeByte(this.action.ordinal());
    }

    @Override
    public IMessage onMessage(final PktEFabricatorGUIAction message, final MessageContext ctx) {
        final EntityPlayerMP player = ctx.getServerHandler().player;
        final Action action = message.action;

        ModularMachinery.EXECUTE_MANAGER.addSyncTask(() -> {
            if (action == Action.SWITCH_GUI) {
                if (player.openContainer instanceof ContainerEFabricatorController efGUI) {
                    final EFabricatorController owner = efGUI.getOwner();
                    final BlockPos pos = owner.getPos();
                    player.openGui(ECOAEExtension.MOD_ID, CommonProxy.GuiType.EFABRICATOR_PATTERN_SEARCH.ordinal(), owner.getWorld(), pos.getX(), pos.getY(), pos.getZ());
                    if (player.openContainer instanceof ContainerEFabricatorPatternSearch efPatternSearch) {
                        ECOAEExtension.NET_CHANNEL.sendTo(
                                new PktEFabricatorPatternSearchGUIUpdate(
                                        PktEFabricatorPatternSearchGUIUpdate.UpdateType.FULL,
                                        EFabricatorPatternData.ofFull(efPatternSearch.getOwner())
                                ),
                                player
                        );
                    }
                    return;
                }

                if (player.openContainer instanceof ContainerEFabricatorPatternSearch efPatternSearch) {
                    EFabricatorController owner = efPatternSearch.getOwner();
                    BlockPos pos = owner.getPos();
                    player.openGui(ECOAEExtension.MOD_ID, CommonProxy.GuiType.EFABRICATOR_CONTROLLER.ordinal(), owner.getWorld(), pos.getX(), pos.getY(), pos.getZ());
                    return;
                }
                return;
            }

            if (!(player.openContainer instanceof ContainerEFabricatorController efGUI)) {
                return;
            }

            EFabricatorController owner = efGUI.getOwner();
            switch (action) {
                case ENABLE_OVERCLOCKING -> owner.setOverclocked(true);
                case DISABLE_OVERCLOCKING -> owner.setOverclocked(false);
                case ENABLE_ACTIVE_COOLANT -> owner.setActiveCooling(true);
                case DISABLE_ACTIVE_COOLANT -> owner.setActiveCooling(false);
            }
        });
        return null;
    }

    public enum Action {
        ENABLE_OVERCLOCKING,
        DISABLE_OVERCLOCKING,

        ENABLE_ACTIVE_COOLANT,
        DISABLE_ACTIVE_COOLANT,

        SWITCH_GUI,
    }

}
