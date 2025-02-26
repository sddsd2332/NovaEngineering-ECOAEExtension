package github.kasuminova.ecoaeextension.common.core;

import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.BlockEStorageController;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class CreativeTabNovaEng extends CreativeTabs {
    public static final CreativeTabNovaEng INSTANCE = new CreativeTabNovaEng();

    private CreativeTabNovaEng() {
        super("ecoaeextension");
    }

    @Nonnull
    @Override
    public ItemStack createIcon() {
        return new ItemStack(BlockEStorageController.L9);
    }

}
