package github.kasuminova.ecoaeextension.common.item.ecalculator;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemECalculatorCellDrive extends ItemBlock {

    public ItemECalculatorCellDrive(final Block block) {
        super(block);
    }

    @Override
    public void addInformation(final ItemStack stack, @Nullable final World worldIn, final List<String> tooltip, final ITooltipFlag flagIn) {
        tooltip.add(I18n.format("novaeng.ecalculator_cell_drive.info.0"));
        tooltip.add(I18n.format("novaeng.ecalculator_cell_drive.info.1"));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

}
