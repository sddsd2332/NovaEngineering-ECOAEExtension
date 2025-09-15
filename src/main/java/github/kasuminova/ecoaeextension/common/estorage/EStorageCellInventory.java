package github.kasuminova.ecoaeextension.common.estorage;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.me.storage.AbstractCellInventory;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.item.estorage.EStorageCell;
import github.kasuminova.ecoaeextension.mixin.ae2.AccessorAbstractCellInventory;
import io.netty.util.internal.ThrowableUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class EStorageCellInventory<T extends IAEStack<T>> extends AbstractCellInventory<T> {
    public static final String ITEM_SLOT = "#";
    public static final String ITEM_SLOT_COUNT = "@";
    public static final String ITEM_TYPE_TAG = "it";
    public static final String ITEM_COUNT_TAG = "ic";

    private final EStorageCell<T> cellType;
    private final IStorageChannel<T> channel;

    @SuppressWarnings("deprecation")
    protected EStorageCellInventory(final EStorageCell<T> cellType, final ItemStack o, final ISaveProvider container) {
        super(cellType, o, container);
        ReflectionHelper.setPrivateValue(AbstractCellInventory.class, this, cellType.getTotalTypes(o), "maxItemTypes", null);
        this.cellType = cellType;
        this.channel = cellType.getChannel();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends IAEStack<T>> ICellInventory<T> createInventory(final ItemStack o, final ISaveProvider container) {
        try {
            if (o == null) {
                throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
            }

            final Item type = o.getItem();
            if (!(type instanceof EStorageCell cellType)) {
                throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
            }

            if (!cellType.isStorageCell(o)) {
                throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
            }

            return new EStorageCellInventory<T>(cellType, o, container);
        } catch (final AppEngException e) {
            ECOAEExtension.log.error(ThrowableUtil.stackTraceToString(e));
            return null;
        }
    }

    private boolean isStorageCell(final T input) {
        if (input instanceof final IAEItemStack stack) {
            final IStorageCell<?> type = getStorageCell(stack.getDefinition());

            return type != null && !type.storableInStorageCell();
        }

        return false;
    }

    private static IStorageCell<?> getStorageCell(final ItemStack input) {
        if (input != null) {
            final Item type = input.getItem();

            if (type instanceof IStorageCell) {
                return (IStorageCell<?>) type;
            }
        }

        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean isCellEmpty(ICellInventory inv) {
        if (inv != null) {
            return inv.getAvailableItems(inv.getChannel().createList()).isEmpty();
        }
        return true;
    }

    protected IItemList<T> getCellItems() {
        if (this.cellItems == null) {
            this.cellItems = this.channel.createList();
            this.loadCellItems();
        }

        return this.cellItems;
    }

    private void loadCellItems() {
        if (this.cellItems == null) {
            this.cellItems = this.channel.createList();
        }

        this.cellItems.resetStatus(); // clears totals and stuff.

        final long types = this.getStoredItemTypes();
        boolean needsUpdate = false;

        AccessorAbstractCellInventory inv = (AccessorAbstractCellInventory) this;
        for (int slot = 0; slot < types; slot++) {
            NBTTagCompound compoundTag = inv.getTagCompound().getCompoundTag(ITEM_SLOT + slot);
            long stackSize = inv.getTagCompound().getLong(ITEM_SLOT_COUNT + slot);
            needsUpdate |= !this.loadCellItem(compoundTag, stackSize);
        }

        if (needsUpdate) {
            this.saveChanges();
        }
    }

    @Override
    public void persist() {
        AccessorAbstractCellInventory inv = (AccessorAbstractCellInventory) this;
        if (inv.getIsPersisted()) {
            return;
        }
        NBTTagCompound tagCompound = inv.getTagCompound();

        long itemCount = 0;

        // add new pretty stuff...
        int x = 0;
        for (final T v : this.cellItems) {
            itemCount += v.getStackSize();

            final NBTTagCompound g = new NBTTagCompound();
            v.writeToNBT(g);
            tagCompound.setTag(ITEM_SLOT + x, g);
            tagCompound.setLong(ITEM_SLOT_COUNT + x, v.getStackSize());

            x++;
        }

        final short oldStoredItems = inv.getStoredItemTypes();

        inv.setStoredItemTypes((short) this.cellItems.size());

        if (this.cellItems.isEmpty()) {
            tagCompound.removeTag(ITEM_TYPE_TAG);
        } else {
            tagCompound.setShort(ITEM_TYPE_TAG, inv.getStoredItemTypes());
        }

        inv.setStoredItemCount(itemCount);
        if (itemCount == 0) {
            tagCompound.removeTag(ITEM_COUNT_TAG);
        } else {
            tagCompound.setLong(ITEM_COUNT_TAG, itemCount);
        }

        // clean any old crusty stuff...
        for (; x >= oldStoredItems && x < inv.getMaxItemTypes(); x++) {
            tagCompound.removeTag(ITEM_SLOT + x);
            tagCompound.removeTag(ITEM_SLOT_COUNT + x);
        }

        inv.setIsPersisted(true);
    }

    @Override
    public T injectItems(T input, Actionable mode, IActionSource src) {
        if (input == null) {
            return null;
        }
        if (input.getStackSize() == 0) {
            return null;
        }

        if (this.cellType.isBlackListed(this.getItemStack(), input)) {
            return input;
        }
        // This is slightly hacky as it expects a read-only access, but fine for now.
        // TODO: Guarantee a read-only access. E.g. provide an isEmpty() method and ensure CellInventory does not write
        // any NBT data for empty cells instead of relying on an empty IItemContainer
        if (isStorageCell(input)) {
            ItemStack cellStack = ((IAEItemStack) input).createItemStack();
            ICellInventoryHandler<?> cellInvHandler = AEApi.instance().registries().cell().getCellInventory(cellStack, null, getStorageCell(cellStack).getChannel());
            if (cellInvHandler != null && !isCellEmpty(cellInvHandler.getCellInv())) {
                return input;
            }
        }

        final T l = this.getCellItems().findPrecise(input);
        if (l != null) {
            final long remainingItemCount = this.getRemainingItemCount();
            if (remainingItemCount <= 0) {
                return input;
            }

            if (input.getStackSize() > remainingItemCount) {
                final T r = input.copy();
                r.setStackSize(r.getStackSize() - remainingItemCount);
                if (mode == Actionable.MODULATE) {
                    l.setStackSize(l.getStackSize() + remainingItemCount);
                    // Update Count.
                    AccessorAbstractCellInventory inv = (AccessorAbstractCellInventory) this;
                    inv.setStoredItemCount(inv.getStoredItemCount() + remainingItemCount);
                    this.saveChangesES();
                }
                return r;
            } else {
                if (mode == Actionable.MODULATE) {
                    long prev = l.getStackSize();
                    l.setStackSize(l.getStackSize() + input.getStackSize());
                    // Update Count.
                    AccessorAbstractCellInventory inv = (AccessorAbstractCellInventory) this;
                    if (prev == 0) {
                        inv.setStoredItemTypes((short) (inv.getStoredItemTypes() + 1));
                    }
                    inv.setStoredItemCount(inv.getStoredItemCount() + input.getStackSize());
                    this.saveChangesES();
                }
                return null;
            }
        }

        // room for new type, and for at least one item!
        if (this.canHoldNewItem()) {
            final long remainingItemCount = this.getRemainingItemCount() - (long) this.getBytesPerType() * this.itemsPerByte;
            if (remainingItemCount > 0) {
                if (input.getStackSize() > remainingItemCount) {
                    final T toReturn = input.copy();
                    toReturn.setStackSize(input.getStackSize() - remainingItemCount);
                    if (mode == Actionable.MODULATE) {
                        final T toWrite = input.copy();
                        toWrite.setStackSize(remainingItemCount);

                        this.cellItems.add(toWrite);

                        // Update Types and Counts.
                        AccessorAbstractCellInventory inv = (AccessorAbstractCellInventory) this;
                        inv.setStoredItemTypes((short) (inv.getStoredItemTypes() + 1));
                        inv.setStoredItemCount(inv.getStoredItemCount() + remainingItemCount);
                        this.saveChangesES();
                    }
                    return toReturn;
                }

                if (mode == Actionable.MODULATE) {
                    this.cellItems.add(input);

                    // Update Types and Counts.
                    AccessorAbstractCellInventory inv = (AccessorAbstractCellInventory) this;
                    inv.setStoredItemTypes((short) (inv.getStoredItemTypes() + 1));
                    inv.setStoredItemCount(inv.getStoredItemCount() + input.getStackSize());
                    this.saveChangesES();
                }

                return null;
            }
        }

        return input;
    }

    @Override
    public T extractItems(T request, Actionable mode, IActionSource src) {
        if (request == null) {
            return null;
        }

        final long size =  request.getStackSize();

        T results = null;

        final T l = this.getCellItems().findPrecise(request);
        if (l != null) {
            results = l.copy();

            if (l.getStackSize() <= size) {
                results.setStackSize(l.getStackSize());
                if (mode == Actionable.MODULATE && l.getStackSize() > 0) {
                    l.setStackSize(0);
                    this.saveChanges();
                }
            } else {
                results.setStackSize(size);
                if (mode == Actionable.MODULATE) {
                    l.setStackSize(l.getStackSize() - size);
                    // Update Count.
                    AccessorAbstractCellInventory inv = (AccessorAbstractCellInventory) this;
                    inv.setStoredItemCount(inv.getStoredItemCount() - size);
                    this.saveChangesES();
                }
            }
        }

        return results;
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return this.channel;
    }

    /**
     * 类似 {@link AbstractCellInventory#saveChanges()}，但是不对 cellItems 进行完全扫描。
     */
    protected void saveChangesES() {
        if (this.container != null) {
            ((AccessorAbstractCellInventory) this).setIsPersisted(false);
            this.container.saveChanges(this);
        } else {
            saveChanges();
        }
    }

    @Override
    protected boolean loadCellItem(NBTTagCompound compoundTag, long stackSize) {
        // Now load the item stack
        final T t;
        try {
            t = this.channel.createFromNBT(compoundTag);
            if (t == null) {
                AELog.warn("Removing item " + compoundTag + " from storage cell because the associated item type couldn't be found.");
                return false;
            }
        } catch (Throwable ex) {
            if (AEConfig.instance().isRemoveCrashingItemsOnLoad()) {
                AELog.warn(ex, "Removing item " + compoundTag + " from storage cell because loading the ItemStack crashed.");
                return false;
            }
            throw ex;
        }

        t.setStackSize(stackSize);
        t.setCraftable(false);

        if (stackSize > 0) {
            this.cellItems.add(t);
        }

        return true;
    }

    @Override
    public long getUsedBytes() {
        final long bytesForItemCount = (this.getStoredItemCount() + this.getUnusedItemCount()) / ((long) this.itemsPerByte * cellType.getByteMultiplier());
        return this.getStoredItemTypes() * this.getBytesPerType() + bytesForItemCount;
    }

    @Override
    public long getRemainingItemCount() {
        final long remaining = this.getFreeBytes() * ((long) this.itemsPerByte * cellType.getByteMultiplier()) + this.getUnusedItemCount();
        return remaining > 0 ? remaining : 0;
    }

    @Override
    public int getUnusedItemCount() {
        final int div = (int) (this.getStoredItemCount() % (8 * cellType.getByteMultiplier()));

        if (div == 0) {
            return 0;
        }

        return (this.itemsPerByte * cellType.getByteMultiplier()) - div;
    }

}
