package github.kasuminova.ecoaeextension.common.item.efabriactor;

import github.kasuminova.ecoaeextension.common.block.ecotech.efabricator.BlockEFabricatorParallelProc;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemEFabricatorParallelProc extends ItemBlock {

    public ItemEFabricatorParallelProc(final BlockEFabricatorParallelProc block) {
        super(block);
    }

    @Override
    public void addInformation(final ItemStack stack, @Nullable final World worldIn, final List<String> tooltip, final ITooltipFlag flagIn) {
        BlockEFabricatorParallelProc proc = (BlockEFabricatorParallelProc) this.block;
        tooltip.add(I18n.format("novaeng.efabricator_parallel_proc.info.0"));
        tooltip.add(I18n.format("novaeng.efabricator_parallel_proc.info.1"));

        tooltip.add(I18n.format("novaeng.efabricator_parallel_proc.modifiers"));
        proc.getModifiers()
                .forEach(modifier -> tooltip.add("  " + modifier.getDesc()));

        tooltip.add(I18n.format("novaeng.efabricator_parallel_proc.overclock_modifiers"));
        proc.getOverclockModifiers()
                .forEach(modifier -> tooltip.add("  " + modifier.getDesc()));

        tooltip.add(I18n.format("novaeng.efabricator_parallel_proc.info.2"));
        tooltip.add(I18n.format("novaeng.efabricator_parallel_proc.info.3"));
        tooltip.add(I18n.format("novaeng.efabricator_parallel_proc.info.4"));
        tooltip.add(I18n.format("novaeng.efabricator_parallel_proc.info.5"));
        tooltip.add(I18n.format("novaeng.efabricator_parallel_proc.info.6"));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

}
