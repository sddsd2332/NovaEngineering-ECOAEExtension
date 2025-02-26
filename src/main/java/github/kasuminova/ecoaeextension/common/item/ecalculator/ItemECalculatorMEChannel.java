package github.kasuminova.ecoaeextension.common.item.ecalculator;

import github.kasuminova.ecoaeextension.common.item.ItemBlockME;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemECalculatorMEChannel extends ItemBlockME {

    public ItemECalculatorMEChannel(final Block block) {
        super(block);
    }

    @Override
    public void addInformation(@Nonnull final ItemStack stack, @Nullable final World worldIn, final List<String> tooltip, @Nonnull final ITooltipFlag flagIn) {
        tooltip.add(I18n.format("novaeng.ecalculator_me_channel.info.0"));
        tooltip.add(I18n.format("novaeng.ecalculator_me_channel.info.1"));
        tooltip.add(I18n.format("novaeng.ecalculator_me_channel.info.2"));
        tooltip.add(I18n.format("novaeng.ecalculator_me_channel.info.3"));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

}
