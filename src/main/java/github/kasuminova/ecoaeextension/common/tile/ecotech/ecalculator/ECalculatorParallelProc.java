package github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator;

import net.minecraft.nbt.NBTTagCompound;

public class ECalculatorParallelProc extends ECalculatorPart {

    public int parallelism = 0;

    public ECalculatorParallelProc() {
    }

    public ECalculatorParallelProc(final int parallelism) {
        this.parallelism = parallelism;
    }

    public int getParallelism() {
        return parallelism;
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);
        this.parallelism = compound.getShort("parallelism");

        updateContainingBlockInfo();
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        compound.setShort("parallelism", (short) this.parallelism);
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
