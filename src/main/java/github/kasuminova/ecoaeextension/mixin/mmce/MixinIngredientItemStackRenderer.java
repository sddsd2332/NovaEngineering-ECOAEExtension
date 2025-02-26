package github.kasuminova.ecoaeextension.mixin.mmce;

import hellfirepvp.modularmachinery.common.integration.ingredient.IngredientItemStackRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(IngredientItemStackRenderer.class)
public class MixinIngredientItemStackRenderer {

    @ModifyConstant(method = "renderRequirementOverlyIntoGUI", constant = @Constant(floatValue = 0.5F), remap = false)
    private static float modifyScale(final float ci) {
        if (ci == 0.5F) {
            return 0.7F;
        }
        return ci;
    }

    @ModifyConstant(method = "renderRequirementOverlyIntoGUI", constant = {@Constant(intValue = 12), @Constant(intValue = 16)}, remap = false)
    private static int modifyXY(final int ci) {
        return switch (ci) {
            case 12 -> 11;
            case 16 -> 15;
            default -> ci;
        };
    }

}
