package github.kasuminova.ecoaeextension.common.handler;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.inventory.AppEngCellInventory;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.container.ContainerEStorageController;
import github.kasuminova.ecoaeextension.common.estorage.EStorageCellHandler;
import github.kasuminova.ecoaeextension.common.network.PktEStorageGUIData;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageCellDrive;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageMEChannel;
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
public class EStorageEventHandler {

    public static final EStorageEventHandler INSTANCE = new EStorageEventHandler();
    
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
        if (!(te instanceof final EStorageCellDrive drive)) {
            return;
        }

        EStorageController controller = drive.getController();
        if (controller != null) {
            EStorageMEChannel channel = controller.getChannel();
            if (channel != null && !canInteract(player, channel)) {
                player.sendMessage(new TextComponentTranslation("novaeng.estorage_cell_drive.player.no_permission"));
                event.setCanceled(true);
                return;
            }
        }

        ItemStack stackInHand = player.getHeldItem(hand);

        AppEngCellInventory inv = drive.getDriveInv();
        ItemStack stackInSlot = inv.getStackInSlot(0);
        if (stackInSlot.isEmpty()) {
            if (stackInHand.isEmpty() || EStorageCellHandler.getHandler(stackInHand) == null) {
                return;
            }
            player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, inv.insertItem(0, stackInHand.copy(), false));
            player.sendMessage(new TextComponentTranslation("novaeng.estorage_cell_drive.player.inserted"));
            event.setCanceled(true);
            return;
        }

        if (!stackInHand.isEmpty()) {
            return;
        }

        player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, inv.extractItem(0, stackInSlot.getCount(), false));
        player.sendMessage(new TextComponentTranslation("novaeng.estorage_cell_drive.player.removed"));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.side == Side.CLIENT) {
            return;
        }
        if (!(event.player instanceof final EntityPlayerMP player)) {
            return;
        }
        if (!(player.openContainer instanceof ContainerEStorageController containerESController)) {
            return;
        }
        World world = player.getEntityWorld();
        int tickExisted = containerESController.getTickExisted();
        containerESController.setTickExisted(tickExisted + 1);
        if (world.getTotalWorldTime() % 20 != 0 && tickExisted > 1) {
            return;
        }
        EStorageController controller = containerESController.getOwner();
        ECOAEExtension.NET_CHANNEL.sendTo(new PktEStorageGUIData(controller), player);
    }

}
