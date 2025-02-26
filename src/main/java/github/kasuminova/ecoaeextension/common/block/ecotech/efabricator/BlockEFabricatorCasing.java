package github.kasuminova.ecoaeextension.common.block.ecotech.efabricator;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import net.minecraft.util.ResourceLocation;

public class BlockEFabricatorCasing extends BlockEFabricator {

    public static final BlockEFabricatorCasing INSTANCE = new BlockEFabricatorCasing();

    protected BlockEFabricatorCasing() {
        this.setDefaultState(this.blockState.getBaseState());
        this.setRegistryName(new ResourceLocation(ECOAEExtension.MOD_ID, "efabricator_casing"));
        this.setTranslationKey(ECOAEExtension.MOD_ID + '.' + "efabricator_casing");
    }

}
