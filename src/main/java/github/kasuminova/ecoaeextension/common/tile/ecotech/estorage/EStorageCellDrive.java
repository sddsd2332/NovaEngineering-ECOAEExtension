package github.kasuminova.ecoaeextension.common.tile.ecotech.estorage;

import appeng.api.AEApi;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.*;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.inventory.AppEngCellInventory;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.BlockEStorageController;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageCapacity;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageLevel;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageType;
import github.kasuminova.ecoaeextension.common.container.data.EStorageCellData;
import github.kasuminova.ecoaeextension.common.estorage.ECellDriveWatcher;
import github.kasuminova.ecoaeextension.common.estorage.EStorageCellHandler;
import github.kasuminova.ecoaeextension.common.item.estorage.EStorageCell;
import github.kasuminova.ecoaeextension.common.item.estorage.EStorageCellFluid;
import github.kasuminova.ecoaeextension.common.item.estorage.EStorageCellGas;
import github.kasuminova.ecoaeextension.common.item.estorage.EStorageCellItem;
import github.kasuminova.ecoaeextension.common.network.PktCellDriveStatusUpdate;
import hellfirepvp.modularmachinery.common.base.Mods;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

import static appeng.helpers.ItemStackHelper.stackFromNBT;
import static appeng.helpers.ItemStackHelper.stackWriteToNBT;

@SuppressWarnings("rawtypes")
public class EStorageCellDrive extends EStoragePart implements ISaveProvider, IAEAppEngInventory {

    protected final AppEngCellInventory driveInv = new AppEngCellInventory(this, 1);
    protected final Map<IStorageChannel<? extends IAEStack<?>>, IMEInventoryHandler<?>> inventoryHandlers = new Reference2ObjectOpenHashMap<>();

    protected EStorageCellHandler cellHandler = null;
    protected ECellDriveWatcher<IAEItemStack> watcher = null;

    protected boolean isCached = false;

    protected long lastWriteTick = 0;
    protected boolean writing = false;

    public EStorageCellDrive() {
        this.driveInv.setFilter(CellInvFilter.INSTANCE);
    }

    public static int getMaxTypes(final EStorageCellData data) {
        return switch (data.type()) {
            case EMPTY -> 0;
            case ITEM -> 315;
            case FLUID -> 25;
            case GAS -> Mods.MEKENG.isPresent() ? 25 : 0;
        };
    }

    public static long getMaxBytes(final EStorageCellData data) {
        DriveStorageType type = data.type();
        DriveStorageLevel level = data.level();
        return switch (type) {
            case EMPTY -> 0;
            case ITEM -> switch (level) {
                case EMPTY -> 0;
                case A -> EStorageCellItem.LEVEL_A.getBytes(ItemStack.EMPTY);
                case B -> EStorageCellItem.LEVEL_B.getBytes(ItemStack.EMPTY);
                case C -> EStorageCellItem.LEVEL_C.getBytes(ItemStack.EMPTY);
            };
            case FLUID -> switch (level) {
                case EMPTY -> 0;
                case A -> EStorageCellFluid.LEVEL_A.getBytes(ItemStack.EMPTY);
                case B -> EStorageCellFluid.LEVEL_B.getBytes(ItemStack.EMPTY);
                case C -> EStorageCellFluid.LEVEL_C.getBytes(ItemStack.EMPTY);
            };
            case GAS -> Mods.MEKENG.isPresent() ? switch (level) {
                case EMPTY -> 0;
                case A -> EStorageCellGas.LEVEL_A.getBytes(ItemStack.EMPTY);
                case B -> EStorageCellGas.LEVEL_B.getBytes(ItemStack.EMPTY);
                case C -> EStorageCellGas.LEVEL_C.getBytes(ItemStack.EMPTY);
            } : 0;
        };
    }

    public void updateWriteState() {
        long totalWorldTime = world.getTotalWorldTime();
        boolean changed = false;
        if (totalWorldTime - lastWriteTick >= 40) {
            if (writing) {
                writing = false;
                changed = true;
            }
        } else if (!writing) {
            writing = true;
            changed = true;
        }
        if (cellHandler == null) {
            return;
        }
        // Static update or changed update.
        if (world.getTotalWorldTime() % 200 == 0) {
            BlockPos pos = getPos();
            ECOAEExtension.NET_CHANNEL.sendToAllTracking(
                    new PktCellDriveStatusUpdate(getPos(), writing),
                    new NetworkRegistry.TargetPoint(
                            world.provider.getDimension(),
                            pos.getX(), pos.getY(), pos.getZ(),
                            -1)
            );
        } else if (changed) {
            BlockPos pos = getPos();
            ECOAEExtension.NET_CHANNEL.sendToAllAround(
                    new PktCellDriveStatusUpdate(getPos(), writing),
                    new NetworkRegistry.TargetPoint(
                            world.provider.getDimension(),
                            pos.getX(), pos.getY(), pos.getZ(),
                            16)
            );
        }
    }

    protected void updateHandler(final boolean refreshState) {
        if (isCached) {
            return;
        }
        watcher = null;
        cellHandler = null;
        inventoryHandlers.clear();
        isCached = true;
        ItemStack stack = driveInv.getStackInSlot(0);
        if (stack.isEmpty()) {
            updateDriveBlockState();
            return;
        }
        if ((cellHandler = EStorageCellHandler.getHandler(stack)) == null) {
            return;
        }
        ICellInventoryHandler cellInventory = null;
        final Collection<IStorageChannel<? extends IAEStack<?>>> storageChannels = AEApi.instance().storage().storageChannels();
        for (final IStorageChannel<? extends IAEStack<?>> channel : storageChannels) {
            cellInventory = cellHandler.getCellInventory(stack, this, channel);
            if (cellInventory == null) {
                continue;
            }
            driveInv.setHandler(0, cellInventory);
            watcher = new ECellDriveWatcher<>(cellInventory, channel, this);
            if (partController != null) {
                watcher.setPriority(partController.getChannel().getPriority());
            }
            inventoryHandlers.put(channel, watcher);
            break;
        }
        if (partController != null) {
            partController.recalculateEnergyUsage();
        }

        if (cellInventory == null || !refreshState) {
            return;
        }
        updateDriveBlockState();
    }

    public boolean isCellSupported(final DriveStorageLevel level) {
        if (partController == null) {
            return false;
        }
        if (level == DriveStorageLevel.A) {
            BlockEStorageController parent = partController.getParentController();
            return parent == BlockEStorageController.L4 || parent == BlockEStorageController.L6 || parent == BlockEStorageController.L9;
        }
        if (level == DriveStorageLevel.B) {
            BlockEStorageController parent = partController.getParentController();
            return parent == BlockEStorageController.L6 || parent == BlockEStorageController.L9;
        }
        if (level == DriveStorageLevel.C) {
            BlockEStorageController parent = partController.getParentController();
            return parent == BlockEStorageController.L9;
        }
        return false;
    }

    public void updateDriveBlockState() {
        if (world == null) {
            return;
        }
        markForUpdate();
    }

    public static DriveStorageType getCellType(final EStorageCell<?> cell) {
        DriveStorageType type;
        if (cell instanceof EStorageCellItem) {
            type = DriveStorageType.ITEM;
        } else if (cell instanceof EStorageCellFluid) {
            type = DriveStorageType.FLUID;
        } else if (Mods.MEKENG.isPresent() && cell instanceof EStorageCellGas) {
            type = DriveStorageType.GAS;
        } else {
            return null;
        }
        return type;
    }

    public static DriveStorageCapacity getCapacity(final ICellInventoryHandler cellInvHandler) {
        if (cellInvHandler == null) {
            return DriveStorageCapacity.EMPTY;
        }
        ICellInventory cellInv = cellInvHandler.getCellInv();
        if (cellInv == null) {
            return DriveStorageCapacity.EMPTY;
        }
        long totalTypes = cellInv.getTotalItemTypes();
        long storedTypes = cellInv.getStoredItemTypes();
        if (storedTypes == 0) {
            return DriveStorageCapacity.EMPTY;
        }
        if (cellInv.getFreeBytes() <= 0) {
            return DriveStorageCapacity.FULL;
        }
        if (storedTypes >= totalTypes) {
            return DriveStorageCapacity.TYPE_MAX;
        }
        return DriveStorageCapacity.EMPTY;
    }

    @SuppressWarnings("unchecked")
    public <T extends IAEStack<T>> IMEInventoryHandler<T> getHandler(final IStorageChannel<T> channel) {
        updateHandler(false);
        if (driveInv.getStackInSlot(0).getItem() instanceof EStorageCell<?> cell && isCellSupported(cell.getLevel())) {
            IMEInventoryHandler<?> handler = inventoryHandlers.get(channel);
            return handler == null ? null : (IMEInventoryHandler<T>) handler;
        }
        return null;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added) {
        this.isCached = false; // recalculate the storage cell.
        this.updateHandler(true);
        this.markForUpdateSync();

        EStorageController controller = getController();
        if (controller == null) {
            return;
        }

        EStorageMEChannel channel = controller.getChannel();
        AENetworkProxy proxy = channel.getProxy();
        IActionSource source = channel.getSource();

        try {
            if (proxy.isActive()) {
                final IStorageGrid gs = proxy.getStorage();
                postChanges(gs, removed, added, source);
            }
            proxy.getGrid().postEvent(new MENetworkCellArrayUpdate());
        } catch (final GridAccessException ignored) {
        }

    }

    @Override
    public void onAssembled() {
        super.onAssembled();
        if (watcher != null) {
            watcher.setPriority(partController.getChannel().getPriority());
        }
    }

    @Override
    public void onDisassembled() {
        super.onDisassembled();
        EStorageController controller = getController();
        if (controller == null) {
            return;
        }
        EStorageMEChannel channel = controller.getChannel();
        if (channel == null) {
            return;
        }
        AENetworkProxy proxy = channel.getProxy();
        IActionSource source = channel.getSource();

        try {
            if (proxy.isActive()) {
                ItemStack removed = driveInv.getStackInSlot(0);
                final IStorageGrid gs = proxy.getStorage();
                postChanges(gs, removed, ItemStack.EMPTY, source);
            }
            proxy.getGrid().postEvent(new MENetworkCellArrayUpdate());
        } catch (final GridAccessException ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    public void postChanges(final IStorageGrid gs, final ItemStack removed, final ItemStack added, final IActionSource src) {
        if (cellHandler == null) {
            return;
        }
        for (final IStorageChannel<?> chan : AEApi.instance().storage().storageChannels()) {
            final IItemList<?> myChanges = chan.createList();

            if (!removed.isEmpty()) {
                final IMEInventory myInv = cellHandler.getCellInventory(removed, null, chan);
                if (myInv != null) {
                    myInv.getAvailableItems(myChanges);
                    for (final IAEStack is : myChanges) {
                        is.setStackSize(-is.getStackSize());
                    }
                }
            }
            if (!added.isEmpty()) {
                final IMEInventory myInv = cellHandler.getCellInventory(added, null, chan);
                if (myInv != null) {
                    myInv.getAvailableItems(myChanges);
                }
            }
            gs.postAlterationOfStoredItems(chan, myChanges, src);
        }
    }

    public AppEngCellInventory getDriveInv() {
        return driveInv;
    }

    public ECellDriveWatcher<IAEItemStack> getWatcher() {
        return watcher;
    }

    public void onWriting() {
        this.lastWriteTick = world.getTotalWorldTime();
    }

    public boolean isWriting() {
        return writing;
    }

    public void setWriting(final boolean writing) {
        this.writing = writing;
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(driveInv);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readCustomNBT(final NBTTagCompound tag) {
        super.readCustomNBT(tag);

        final NBTTagCompound opt = tag.getCompoundTag("driveInv");
        for (int x = 0; x < driveInv.getSlots(); x++) {
            final NBTTagCompound item = opt.getCompoundTag("item" + x);
            ItemHandlerUtil.setStackInSlot(driveInv, x, stackFromNBT(item));
        }

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            notifyUpdate();
        }
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound tag) {
        super.writeCustomNBT(tag);

        final NBTTagCompound opt = new NBTTagCompound();
        for (int x = 0; x < driveInv.getSlots(); x++) {
            final NBTTagCompound itemNBT = new NBTTagCompound();
            final ItemStack is = driveInv.getStackInSlot(x);
            if (!is.isEmpty()) {
                stackWriteToNBT(is, itemNBT);
            }
            opt.setTag("item" + x, itemNBT);
        }
        tag.setTag("driveInv", opt);
    }

    @Override
    public void saveChanges(@Nullable final ICellInventory<?> cellInventory) {
        saveChanges();
    }

    @Override
    public void saveChanges() {
        markDirty();
    }

    @Override
    public void markDirty() {
        markChunkDirty();
    }

    private static class CellInvFilter implements IAEItemFilter {

        private static final CellInvFilter INSTANCE = new CellInvFilter();

        @Override
        public boolean allowExtract(IItemHandler inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
            return !stack.isEmpty() && EStorageCellHandler.getHandler(stack) != null;
        }

    }
}
