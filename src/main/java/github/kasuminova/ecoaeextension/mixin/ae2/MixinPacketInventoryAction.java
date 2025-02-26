package github.kasuminova.ecoaeextension.mixin.ae2;

import appeng.container.AEBaseContainer;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import github.kasuminova.ecoaeextension.common.handler.AEPktInvActionSpamHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketInventoryAction.class)
public class MixinPacketInventoryAction {

    @Shadow(remap = false) @Final
    private InventoryAction action;

    @Inject(method = "serverPacketData", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectServerPacketData(final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player, final CallbackInfo ci) {
        final EntityPlayerMP sender = (EntityPlayerMP) player;
        if (!(sender.openContainer instanceof AEBaseContainer)) {
            return;
        }
        if (action == InventoryAction.MOVE_REGION && AEPktInvActionSpamHandler.receivePacketAndCheckSpam(sender)) {
            sender.connection.disconnect(new TextComponentString("[NovaEng-Core]" + TextFormatting.RED + " Disconnected by AE2 Packet Spam."));
            ci.cancel();
        }
    }

}
