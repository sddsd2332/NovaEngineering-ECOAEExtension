package github.kasuminova.ecoaeextension.common.handler;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.container.ContainerEFabricatorController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorController;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@SuppressWarnings("MethodMayBeStatic")
public class EFabricatorEventHandler {

    public static final EFabricatorEventHandler INSTANCE = new EFabricatorEventHandler();
    public static final int UPDATE_INTERVAL = 10;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.side == Side.CLIENT) {
            return;
        }
        if (!(event.player instanceof final EntityPlayerMP player)) {
            return;
        }
        if (!(player.openContainer instanceof ContainerEFabricatorController containerEFController)) {
            return;
        }
        World world = player.getEntityWorld();
        int tickExisted = containerEFController.getTickExisted();
        containerEFController.setTickExisted(tickExisted + 1);
        if (world.getTotalWorldTime() % UPDATE_INTERVAL != 0 && tickExisted > 1) {
            return;
        }
        EFabricatorController controller = containerEFController.getOwner();
        ECOAEExtension.NET_CHANNEL.sendTo(controller.getGuiDataPacket(), player);
    }

}
