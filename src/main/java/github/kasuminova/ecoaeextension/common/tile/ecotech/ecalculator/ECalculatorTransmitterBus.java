package github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator;

import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.BlockECalculatorCellDrive;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.BlockECalculatorTransmitterBus;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.Levels;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.TransmitterBusLinkLevel;
import github.kasuminova.ecoaeextension.common.block.prop.FacingProp;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ECalculatorTransmitterBus extends ECalculatorPart {

    protected boolean upConnected = false;
    protected boolean downConnected = false;

    @Override
    public void onAssembled() {
        super.onAssembled();
        connectCellDrives();
    }

    @Override
    public void onDisassembled() {
        super.onDisassembled();
        disconnectCellDrives();
    }

    protected void connectCellDrives() {
        boolean changed = false;

        if (connectCellDrive(EnumFacing.UP)) {
            if (!this.upConnected) {
                changed = true;
                this.upConnected = true;
            }
        } else if (this.upConnected) {
            changed = true;
            this.upConnected = false;
        }
        if (connectCellDrive(EnumFacing.DOWN)) {
            if (!this.downConnected) {
                changed = true;
                this.downConnected = true;
            }
        } else if (this.downConnected) {
            changed = true;
            this.downConnected = false;
        }

        if (changed) {
            markForUpdateSync();
        }
    }

    protected void disconnectCellDrives() {
        boolean changed = false;

        if (this.upConnected && disconnectCellDrive(EnumFacing.UP)) {
            changed = true;
            this.upConnected = false;
        }
        if (this.downConnected && disconnectCellDrive(EnumFacing.DOWN)) {
            changed = true;
            this.downConnected = false;
        }

        if (changed) {
            markForUpdateSync();
        }
    }

    protected boolean disconnectCellDrive(final EnumFacing disconnectFacing) {
        World world = getWorld();
        BlockPos disconnectPos = getPos().offset(disconnectFacing);
        IBlockState disconnectBlock = world.getBlockState(disconnectPos);
        if (!(disconnectBlock.getBlock() instanceof BlockECalculatorCellDrive)) {
            return false;
        }

        EnumFacing facing = disconnectBlock.getValue(FacingProp.HORIZONTALS);
        IBlockState thisBlock = world.getBlockState(getPos());
        if (!(thisBlock.getBlock() instanceof BlockECalculatorTransmitterBus)) {
            return false;
        }

        EnumFacing currentFacing = thisBlock.getValue(FacingProp.HORIZONTALS);
        if (facing != currentFacing) {
            return false;
        }

        if (!(world.getTileEntity(disconnectPos) instanceof ECalculatorCellDrive drive)) {
            return false;
        }

        drive.disconnectTransmitter();
        return true;
    }

    protected boolean connectCellDrive(final EnumFacing connectFacing) {
        World world = getWorld();
        BlockPos connectPos = getPos().offset(connectFacing);
        IBlockState connectBlock = world.getBlockState(connectPos);
        if (!(connectBlock.getBlock() instanceof BlockECalculatorCellDrive)) {
            return false;
        }

        EnumFacing facing = connectBlock.getValue(FacingProp.HORIZONTALS);
        IBlockState thisBlock = world.getBlockState(getPos());
        if (!(thisBlock.getBlock() instanceof BlockECalculatorTransmitterBus)) {
            return false;
        }

        EnumFacing currentFacing = thisBlock.getValue(FacingProp.HORIZONTALS);
        if (facing != currentFacing) {
            return false;
        }

        if (!(world.getTileEntity(connectPos) instanceof ECalculatorCellDrive drive)) {
            return false;
        }

        return drive.connectTransmitter(connectFacing.getOpposite(), getControllerLevel());
    }

    public void neighborChanged(final BlockPos changedPos) {
        if (getController() != null) {
            if (changedPos.equals(getPos().up()) || changedPos.equals(getPos().down())) {
                connectCellDrives();
            }
        } else {
            disconnectCellDrives();
        }
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);

        this.upConnected = compound.getBoolean("upConnected");
        this.downConnected = compound.getBoolean("downConnected");

        updateContainingBlockInfo();
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        compound.setBoolean("upConnected", this.upConnected);
        compound.setBoolean("downConnected", this.downConnected);
    }

    public TransmitterBusLinkLevel getLinkLevel() {
        final Levels controllerLevel = getControllerLevel();
        if (controllerLevel == null) {
            return TransmitterBusLinkLevel.NONE;
        }

        switch (controllerLevel) {
            case L4 -> {
                return TransmitterBusLinkLevel.L4;
            }
            case L6 -> {
                return TransmitterBusLinkLevel.L6;
            }
            case L9 -> {
                return TransmitterBusLinkLevel.L9;
            }
        }

        return TransmitterBusLinkLevel.NONE;
    }

    public boolean isAllConnected() {
        return this.upConnected && this.downConnected;
    }

    public boolean isUpConnected() {
        return this.upConnected;
    }

    public boolean isDownConnected() {
        return this.downConnected;
    }

}
