package github.kasuminova.ecoaeextension.common.tile.ecotech;

import hellfirepvp.modularmachinery.common.tiles.base.TileEntitySynchronized;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractEPart<C extends EPartController<?>> extends TileEntitySynchronized implements EPart<C> {

    protected C partController = null;
    protected boolean loaded = false;

    public void setController(final EPartController<?> storageController) {
        this.partController = (C) storageController;
    }

    @Nullable
    public C getController() {
        return partController;
    }

    public void onAssembled() {
    }

    public void onDisassembled() {
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.loaded = true;
    }

    @Override
    public void onChunkUnload() {
        loaded = false;
        super.onChunkUnload();
        if (partController != null) {
            partController.disassemble();
        }
    }

    @Override
    public void invalidate() {
        loaded = false;
        super.invalidate();
        if (partController != null) {
            partController.disassemble();
        }
    }

    @Override
    public boolean shouldRefresh(@Nonnull final World world, @Nonnull final BlockPos pos, final IBlockState oldState, final IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);
    }

    /**
     * Refresh client block state to actual state.
     */
    @Override
    @SuppressWarnings("ConstantValue")
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
        final World world = getWorld();
        if (FMLCommonHandler.instance().getEffectiveSide().isClient() && world != null) {
            final BlockPos pos = this.pos;
            final IBlockState state = world.getBlockState(pos);
            final IBlockState actual = state.getActualState(world, pos);
            world.setBlockState(pos, actual);
        }
    }

}
