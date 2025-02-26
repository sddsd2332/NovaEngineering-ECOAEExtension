package github.kasuminova.ecoaeextension.common.item.ecalculator;

import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.BlockECalculatorParallelProc;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemECalculatorParallelProc extends ItemBlock {

    public ItemECalculatorParallelProc(final Block block) {
        super(block);
    }

    @Override
    public void addInformation(@Nonnull final ItemStack stack, @Nullable final World worldIn, final List<String> tooltip, @Nonnull final ITooltipFlag flagIn) {
        BlockECalculatorParallelProc proc = (BlockECalculatorParallelProc) this.block;
        tooltip.add(I18n.format("novaeng.ecalculator_parallel_proc.info.0"));
        tooltip.add(I18n.format("novaeng.ecalculator_parallel_proc.info.1"));
        tooltip.add(I18n.format("novaeng.ecalculator_parallel_proc.modifiers"));
        tooltip.add(I18n.format("novaeng.ecalculator_parallel_proc.modifier.add", proc.getParallelism()));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

}
