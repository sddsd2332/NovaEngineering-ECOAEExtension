package github.kasuminova.ecoaeextension.mixin.nae2;

import appeng.api.AEApi;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellRegistry;
import co.neeve.nae2.common.integration.jei.JEICellCategory;
import github.kasuminova.ecoaeextension.common.estorage.EStorageCellHandler;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(JEICellCategory.class)
public class MixinJEICellCategory {

    @Redirect(
            method = "getCellInfo",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/storage/ICellRegistry;getHandler(Lnet/minecraft/item/ItemStack;)Lappeng/api/storage/ICellHandler;",
                    remap = false
            ),
            remap = false
    )
    private static ICellHandler redirectGetEStorageCellHandler(final ICellRegistry instance, final ItemStack stack) {
        EStorageCellHandler handler = EStorageCellHandler.getHandler(stack);
        if (handler != null) {
            return handler;
        }
        return AEApi.instance().registries().cell().getHandler(stack);
    }

}
