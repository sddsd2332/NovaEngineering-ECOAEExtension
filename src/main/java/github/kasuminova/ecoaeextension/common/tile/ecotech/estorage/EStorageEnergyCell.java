package github.kasuminova.ecoaeextension.common.tile.ecotech.estorage;

import appeng.api.config.Actionable;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.BlockEStorageEnergyCell;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.EnergyCellStatus;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class EStorageEnergyCell extends EStoragePart implements Comparable<EStorageEnergyCell> {

    protected double energyStored = 0D;
    protected double maxEnergyStore = 0D;

    protected boolean recalculateCap = false;

    protected EnergyCellStatus currentStatus = EnergyCellStatus.EMPTY;

    public EStorageEnergyCell() {
    }

    public EStorageEnergyCell(final double maxEnergyStore) {
        this.maxEnergyStore = maxEnergyStore;
    }

    public void recalculateCapacity() {
        recalculateCap = false;

        double fillFactor = getFillFactor();
        EnergyCellStatus newStatus = getStatusFromFillFactor(fillFactor);
        if (newStatus == currentStatus) {
            return;
        }
        currentStatus = newStatus;

        if (world == null) {
            return;
        }
        markForUpdateSync();
    }

    public double injectPower(final double amt, final Actionable mode) {
        if (mode == Actionable.SIMULATE) {
            final double fakeBattery = energyStored + amt;
            if (fakeBattery > maxEnergyStore) {
                return fakeBattery - maxEnergyStore;
            }
            return 0;
        }

        if (amt < 0.000001) {
            return 0;
        }
        if (energyStored >= maxEnergyStore) {
            return amt;
        }

        final double maxCanInsert = maxEnergyStore - energyStored;
        final double toInsert = Math.min(amt, maxCanInsert);

        energyStored += toInsert;
        recalculateCap = true;
        return amt - toInsert;
    }

    public double extractPower(final double amt, final Actionable mode) {
        if (mode == Actionable.SIMULATE) {
            return Math.min(this.energyStored, amt);
        }

        if (energyStored <= 0) {
            return 0;
        }

        final double maxCanExtract = energyStored;
        final double toExtract = Math.min(amt, maxCanExtract);

        energyStored -= toExtract;
        recalculateCap = true;
        return toExtract;
    }

    public double getEnergyStored() {
        return energyStored;
    }

    public void setEnergyStored(final double energyStored) {
        this.energyStored = energyStored;
    }

    public double getMaxEnergyStore() {
        return maxEnergyStore;
    }

    public double getFillFactor() {
        return maxEnergyStore == 0 ? 0 : energyStored / maxEnergyStore;
    }

    public boolean shouldRecalculateCap() {
        return recalculateCap;
    }

    @Override
    public void readCustomNBT(final NBTTagCompound tag) {
        super.readCustomNBT(tag);
        energyStored = tag.getDouble("energyStored");
        maxEnergyStore = tag.getDouble("maxEnergyStore");
        currentStatus = getStatusFromFillFactor(getFillFactor());

        if (FMLCommonHandler.instance().getEffectiveSide().isClient() && world != null) {
            IBlockState current = world.getBlockState(pos);
            if (current.getBlock() instanceof BlockEStorageEnergyCell) {
                world.setBlockState(pos, current.withProperty(EnergyCellStatus.STATUS, EStorageEnergyCell.getStatusFromFillFactor(getFillFactor())));
            }
        }
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound tag) {
        super.writeCustomNBT(tag);
        tag.setDouble("energyStored", energyStored);
        tag.setDouble("maxEnergyStore", maxEnergyStore);
    }

    @Override
    public int compareTo(final EStorageEnergyCell o) {
        return Double.compare(o.energyStored, energyStored);
    }

    public static EnergyCellStatus getStatusFromFillFactor(final double fillFactor) {
        EnergyCellStatus status;
        if (fillFactor >= 0.9D) {
            status = EnergyCellStatus.FULL;
        } else if (fillFactor >= 0.7D) {
            status = EnergyCellStatus.HIGH;
        } else if (fillFactor >= 0.5D) {
            status = EnergyCellStatus.MID;
        } else if (fillFactor >= 0.05D) {
            status = EnergyCellStatus.LOW;
        } else {
            status = EnergyCellStatus.EMPTY;
        }
        return status;
    }

}
