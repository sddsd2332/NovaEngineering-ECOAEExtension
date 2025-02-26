package github.kasuminova.ecoaeextension.common.item.ecalculator;

import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.BlockECalculatorThreadCore;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.BlockECalculatorThreadCoreHyper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemECalculatorThreadCore extends ItemBlock {

    public ItemECalculatorThreadCore(final BlockECalculatorThreadCore block) {
        super(block);
        block.setItem(this);
    }

    @Override
    public void addInformation(final ItemStack stack, @Nullable final World worldIn, final List<String> tooltip, final ITooltipFlag flagIn) {
        BlockECalculatorThreadCore threadCore = (BlockECalculatorThreadCore) this.block;
        if (threadCore instanceof BlockECalculatorThreadCoreHyper) {
            tooltip.add(I18n.format("novaeng.ecalculator_thread_core_hyper.info.0"));
            tooltip.add(I18n.format("novaeng.ecalculator_thread_core_hyper.info.1"));
            tooltip.add(I18n.format("novaeng.ecalculator_thread_core_hyper.info.2"));
            tooltip.add(I18n.format("novaeng.ecalculator_thread_core.modifiers"));
            tooltip.add(I18n.format("novaeng.ecalculator_thread_core.modifier.add", threadCore.getThreads()));
            tooltip.add(I18n.format("novaeng.ecalculator_thread_core_hyper.modifier.add", threadCore.getHyperThreads()));
        } else {
            tooltip.add(I18n.format("novaeng.ecalculator_thread_core.info.0"));
            tooltip.add(I18n.format("novaeng.ecalculator_thread_core.info.1"));
            tooltip.add(I18n.format("novaeng.ecalculator_thread_core.info.2"));
            tooltip.add(I18n.format("novaeng.ecalculator_thread_core.modifiers"));
            tooltip.add(I18n.format("novaeng.ecalculator_thread_core.modifier.add", threadCore.getThreads()));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

}
