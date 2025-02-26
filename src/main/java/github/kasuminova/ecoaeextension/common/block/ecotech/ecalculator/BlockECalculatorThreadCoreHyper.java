package github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import net.minecraft.util.ResourceLocation;

public class BlockECalculatorThreadCoreHyper extends BlockECalculatorThreadCore {

    public static final BlockECalculatorThreadCoreHyper L4 = new BlockECalculatorThreadCoreHyper("l4", 0, 2);
    public static final BlockECalculatorThreadCoreHyper L6 = new BlockECalculatorThreadCoreHyper("l6", 0, 4);
    public static final BlockECalculatorThreadCoreHyper L9 = new BlockECalculatorThreadCoreHyper("l9", 1, 8);

    protected BlockECalculatorThreadCoreHyper(final String level, final int threads, final int hyperThreads) {
        super(
                new ResourceLocation(ECOAEExtension.MOD_ID, "ecalculator_thread_core_hyper_" + level),
                ECOAEExtension.MOD_ID + '.' + "ecalculator_thread_core_hyper_" + level,
                threads, hyperThreads
        );
    }

}
