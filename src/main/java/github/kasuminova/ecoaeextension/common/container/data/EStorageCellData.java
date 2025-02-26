package github.kasuminova.ecoaeextension.common.container.data;

import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.tile.inventory.AppEngCellInventory;
import com.github.bsideup.jabel.Desugar;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageLevel;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageType;
import github.kasuminova.ecoaeextension.common.estorage.ECellDriveWatcher;
import github.kasuminova.ecoaeextension.common.estorage.EStorageCellHandler;
import github.kasuminova.ecoaeextension.common.item.estorage.EStorageCell;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageCellDrive;
import net.minecraft.item.ItemStack;

@Desugar
public record EStorageCellData(DriveStorageType type, DriveStorageLevel level, int usedTypes, long usedBytes) {
    
    public static EStorageCellData from(final EStorageCellDrive drive) {
        AppEngCellInventory driveInv = drive.getDriveInv();
        ItemStack stack = driveInv.getStackInSlot(0);
        if (stack.isEmpty()) {
            return null;
        }
        EStorageCellHandler handler = EStorageCellHandler.getHandler(stack);
        if (handler == null) {
            return null;
        }
        EStorageCell<?> cell = (EStorageCell<?>) stack.getItem();
        DriveStorageType type = EStorageCellDrive.getCellType(cell);
        if (type == null) {
            return null;
        }
        DriveStorageLevel level = cell.getLevel();
        ECellDriveWatcher<IAEItemStack> watcher = drive.getWatcher();
        if (watcher == null) {
            return null;
        }
        ICellInventoryHandler<?> cellInventory = (ICellInventoryHandler<?>) watcher.getInternal();
        if (cellInventory == null) {
            return null;
        }
        ICellInventory<?> cellInv = cellInventory.getCellInv();
        if (cellInv == null) {
            return null;
        }
        long storedTypes = cellInv.getStoredItemTypes();
        long usedBytes = cellInv.getUsedBytes();
        return new EStorageCellData(type, level, (int) storedTypes, usedBytes);
    }
    
}
