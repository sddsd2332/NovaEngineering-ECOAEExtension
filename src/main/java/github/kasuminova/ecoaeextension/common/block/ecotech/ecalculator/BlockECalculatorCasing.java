package github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import net.minecraft.util.ResourceLocation;

public class BlockECalculatorCasing extends BlockECalculator {

    public static final BlockECalculatorCasing INSTANCE = new BlockECalculatorCasing();

    protected BlockECalculatorCasing() {
        this.setDefaultState(this.blockState.getBaseState());
        this.setRegistryName(new ResourceLocation(ECOAEExtension.MOD_ID, "ecalculator_casing"));
        this.setTranslationKey(ECOAEExtension.MOD_ID + '.' + "ecalculator_casing");
    }

}
