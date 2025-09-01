package github.kasuminova.ecoaeextension.common.block.ecotech.estorage;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import net.minecraft.util.ResourceLocation;

public class BlockEStorageCasing extends BlockEStorage {

    public static final BlockEStorageCasing INSTANCE = new BlockEStorageCasing();

    protected BlockEStorageCasing() {
        this.setDefaultState(this.blockState.getBaseState());
        this.setRegistryName(new ResourceLocation(ECOAEExtension.MOD_ID, "estorage_casing"));
        this.setTranslationKey(ECOAEExtension.MOD_ID + '.' + "estorage_casing");
    }

}
