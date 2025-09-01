package github.kasuminova.ecoaeextension.common.item.efabriactor;

import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.item.ItemBlockController;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemEFabricatorController extends ItemBlockController {

    public ItemEFabricatorController(final BlockController ctrlBlock) {
        super(ctrlBlock);
    }

    @Override
    public void addInformation(final ItemStack stack, @Nullable final World worldIn, final List<String> tooltip, final ITooltipFlag flagIn) {
        tooltip.add(I18n.format("novaeng.extendable_fabricator_subsystem.info.0"));
        tooltip.add(I18n.format("novaeng.extendable_fabricator_subsystem.info.1"));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull final ItemStack stack) {
        return net.minecraft.util.text.translation.I18n.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name").trim();
    }

}

