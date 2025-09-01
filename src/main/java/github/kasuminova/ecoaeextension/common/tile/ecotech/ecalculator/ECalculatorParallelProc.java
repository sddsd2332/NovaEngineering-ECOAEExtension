package github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator;

import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.BlockECalculatorParallelProc;
import net.minecraft.block.Block;

public class ECalculatorParallelProc extends ECalculatorPart {

    private Block block;

    public ECalculatorParallelProc() {

    }

    public int getParallelism() {
        return ((BlockECalculatorParallelProc) this.getBlock()).getParallelism();
    }

    public Block getBlock() {
        if (this.block == null && this.world != null) {
            this.block = this.world.getBlockState(this.pos).getBlock();
        }

        return this.block;
    }

    @Override
    public void onDisassembled() {
        super.onDisassembled();
        markForUpdateSync();
    }

    @Override
    public void onAssembled() {
        super.onAssembled();
        markForUpdateSync();
    }

}
