package github.kasuminova.ecoaeextension.common.item;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.core.CreativeTabNovaEng;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class ItemModularServer extends Item {

    public ItemModularServer(final String registryName) {
        setMaxStackSize(1);
        setCreativeTab(CreativeTabNovaEng.INSTANCE);
        setRegistryName(new ResourceLocation(ECOAEExtension.MOD_ID, registryName)).setTranslationKey(ECOAEExtension.MOD_ID + '.' + registryName);;
    }

}
