package github.kasuminova.ecoaeextension.mixin.ae2;

import github.kasuminova.ecoaeextension.common.item.estorage.EStorageCell;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(targets = "appeng.tile.storage.TileDrive$CellValidInventoryFilter")
public class MixinTileDriveFilter {

    /**
     * 不是，那么大个驱动器怎么塞得下那么大个硬盘？
     */
    @Inject(method = "allowInsert", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectEStorageCellCheck(final IItemHandler inv, final int slot, final ItemStack stack, final CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof EStorageCell<?>) {
            cir.setReturnValue(false);
        }
    }

}
