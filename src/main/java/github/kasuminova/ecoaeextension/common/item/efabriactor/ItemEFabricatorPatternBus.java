package github.kasuminova.ecoaeextension.common.item.efabriactor;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemEFabricatorPatternBus extends ItemBlock {

    public ItemEFabricatorPatternBus(final Block block) {
        super(block);
    }

    @Override
    public void addInformation(@Nonnull final ItemStack stack, @Nullable final World worldIn, final List<String> tooltip, @Nonnull final ITooltipFlag flagIn) {
        tooltip.add(I18n.format("novaeng.efabricator_pattern_bus.info.0"));
        tooltip.add(I18n.format("novaeng.efabricator_pattern_bus.info.1"));
        tooltip.add(I18n.format("novaeng.efabricator_pattern_bus.info.2"));
        tooltip.add(I18n.format("novaeng.efabricator_pattern_bus.info.3"));
        tooltip.add(I18n.format("novaeng.efabricator_pattern_bus.info.4"));
        tooltip.add(I18n.format("novaeng.efabricator_pattern_bus.info.5"));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

}
