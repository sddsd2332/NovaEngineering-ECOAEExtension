package github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator;

import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.Levels;
import github.kasuminova.ecoaeextension.common.tile.ecotech.AbstractEPart;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

public abstract class ECalculatorPart extends AbstractEPart<ECalculatorController> {
    
    protected Levels controllerLevel = null;

    @Nullable
    public Levels getControllerLevel() {
        return controllerLevel;
    }

    @Override
    public void onDisassembled() {
        super.onDisassembled();
        controllerLevel = null;
    }

    @Override
    public void onAssembled() {
        super.onAssembled();
        controllerLevel = partController.getLevel();
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);

        if (compound.hasKey("controllerLevel")) {
            this.controllerLevel = Levels.values()[compound.getByte("controllerLevel")];
        } else {
            this.controllerLevel = null;
        }
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        if (this.controllerLevel != null) {
            compound.setByte("controllerLevel", (byte) this.controllerLevel.ordinal());
        }
    }

}
