package github.kasuminova.ecoaeextension.common.handler;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.inventory.AppEngInternalInventory;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.container.ContainerECalculatorController;
import github.kasuminova.ecoaeextension.common.item.ecalculator.ECalculatorCell;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorCellDrive;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorMEChannel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@SuppressWarnings("MethodMayBeStatic")
public class ECalculatorEventHandler {

    public static final ECalculatorEventHandler INSTANCE = new ECalculatorEventHandler();

    public static final int UPDATE_INTERVAL = 10;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.side == Side.CLIENT) {
            return;
        }
        if (!(event.player instanceof final EntityPlayerMP player)) {
            return;
        }
        if (!(player.openContainer instanceof ContainerECalculatorController containerECController)) {
            return;
        }
        World world = player.getEntityWorld();
        int tickExisted = containerECController.getTickExisted();
        containerECController.setTickExisted(tickExisted + 1);
        if (world.getTotalWorldTime() % UPDATE_INTERVAL != 0 && tickExisted > 1) {
            return;
        }
        ECalculatorController controller = containerECController.getOwner();
        ECOAEExtension.NET_CHANNEL.sendTo(controller.getGuiDataPacket(), player);
    }

    private static boolean canInteract(final EntityPlayer player, final IGridProxyable proxyable) {
        final IGridNode gn = proxyable.getProxy().getNode();
        if (gn != null) {
            final IGrid g = gn.getGrid();
            final IEnergyGrid eg = g.getCache(IEnergyGrid.class);
            if (!eg.isNetworkPowered()) {
                return true;
            }

            final ISecurityGrid sg = g.getCache(ISecurityGrid.class);
            return sg.hasPermission(player, SecurityPermissions.BUILD);
        }
        return true;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }

        EnumHand hand = event.getHand();
        if (hand != EnumHand.MAIN_HAND) {
            return;
        }

        EntityPlayer player = event.getEntityPlayer();
        if (!player.isSneaking()) {
            return;
        }

        TileEntity te = world.getTileEntity(event.getPos());
        if (!(te instanceof final ECalculatorCellDrive drive)) {
            return;
        }

        ECalculatorController controller = drive.getController();
        if (controller != null) {
            ECalculatorMEChannel channel = controller.getChannel();
            if (channel != null && !canInteract(player, channel)) {
                player.sendMessage(new TextComponentTranslation("novaeng.ecalculator_cell_drive.player.no_permission"));
                event.setCanceled(true);
                return;
            }
        }

        ItemStack stackInHand = player.getHeldItem(hand);

        AppEngInternalInventory inv = drive.getDriveInv();
        ItemStack stackInSlot = inv.getStackInSlot(0);
        if (stackInSlot.isEmpty()) {
            if (stackInHand.isEmpty() || !(stackInHand.getItem() instanceof ECalculatorCell)) {
                return;
            }
            player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, inv.insertItem(0, stackInHand.copy(), false));
            player.sendMessage(new TextComponentTranslation("novaeng.ecalculator_cell_drive.player.inserted"));
            event.setCanceled(true);
            return;
        }

        if (!stackInHand.isEmpty()) {
            return;
        }

        player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, inv.extractItem(0, stackInSlot.getCount(), false));
        player.sendMessage(new TextComponentTranslation("novaeng.ecalculator_cell_drive.player.removed"));
        event.setCanceled(true);
    }

}
