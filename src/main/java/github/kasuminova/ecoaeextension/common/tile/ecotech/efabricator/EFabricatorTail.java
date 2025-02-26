package github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator;

import net.minecraft.nbt.NBTTagCompound;

public class EFabricatorTail extends EFabricatorPart {

    protected boolean formed = false;

    public EFabricatorTail() {
    }

    public boolean isFormed() {
        return formed;
    }

    @Override
    public void onAssembled() {
        if (!formed) {
            formed = true;
            markForUpdateSync();
        }
        super.onAssembled();
    }

    @Override
    public void onDisassembled() {
        if (formed) {
            formed = false;
            markForUpdateSync();
        }
        super.onDisassembled();
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        formed = compound.getBoolean("formed");
        super.readCustomNBT(compound);
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        compound.setBoolean("formed", formed);
        super.writeCustomNBT(compound);
    }

}
