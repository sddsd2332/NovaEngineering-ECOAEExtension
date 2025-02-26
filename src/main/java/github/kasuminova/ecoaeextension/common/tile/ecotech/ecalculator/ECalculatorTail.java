package github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator;

import net.minecraft.nbt.NBTTagCompound;

public class ECalculatorTail extends ECalculatorPart {

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

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);
        updateContainingBlockInfo();
    }

}
