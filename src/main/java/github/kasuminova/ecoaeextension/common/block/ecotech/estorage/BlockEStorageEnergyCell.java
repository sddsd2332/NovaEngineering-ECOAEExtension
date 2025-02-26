package github.kasuminova.ecoaeextension.common.block.ecotech.estorage;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.EnergyCellStatus;
import github.kasuminova.ecoaeextension.common.block.prop.FacingProp;
import github.kasuminova.ecoaeextension.common.core.CreativeTabNovaEng;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageEnergyCell;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class BlockEStorageEnergyCell extends BlockEStoragePart {
    public static final BlockEStorageEnergyCell L4 = new BlockEStorageEnergyCell("l4", 10_000_000D);
    public static final BlockEStorageEnergyCell L6 = new BlockEStorageEnergyCell("l6", 100_000_000D);
    public static final BlockEStorageEnergyCell L9 = new BlockEStorageEnergyCell("l9", 1_000_000_000D);

    protected final double maxEnergyStore;

    protected BlockEStorageEnergyCell(final String level, double maxEnergyStore) {
        super(Material.IRON);
        this.setHardness(20.0F);
        this.setResistance(2000.0F);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 2);
        this.setCreativeTab(CreativeTabNovaEng.INSTANCE);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FacingProp.HORIZONTALS, EnumFacing.NORTH)
                .withProperty(EnergyCellStatus.STATUS, EnergyCellStatus.EMPTY)
        );
        this.setRegistryName(new ResourceLocation(ECOAEExtension.MOD_ID, "estorage_energy_cell_" + level));
        this.setTranslationKey(ECOAEExtension.MOD_ID + '.' + "estorage_energy_cell_" + level);
        this.maxEnergyStore = maxEnergyStore;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull final World world, @Nonnull final IBlockState state) {
        return new EStorageEnergyCell(maxEnergyStore);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull final World world, final int meta) {
        return new EStorageEnergyCell(maxEnergyStore);
    }

    @Override
    public int getLightValue(@Nonnull final IBlockState state) {
        return state.getValue(EnergyCellStatus.STATUS).ordinal() * 2;
    }

    @Override
    public void dropBlockAsItemWithChance(@Nonnull final World worldIn, @Nonnull final BlockPos pos, @Nonnull final IBlockState state, final float chance, final int fortune) {
    }

    @Override
    public void breakBlock(final World world,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state)
    {
        ItemStack dropped = new ItemStack(Item.getItemFromBlock(this));
        if (dropped.isEmpty()) {
            super.dropBlockAsItemWithChance(world, pos, state, 1.0F, 0);
            world.removeTileEntity(pos);
            return;
        }
        if (!(world.getTileEntity(pos) instanceof final EStorageEnergyCell cell)) {
            super.dropBlockAsItemWithChance(world, pos, state, 1.0F, 0);
            world.removeTileEntity(pos);
            return;
        }

        NBTTagCompound tag = new NBTTagCompound();
        cell.writeCustomNBT(tag);
        cell.setEnergyStored(0D);
        dropped.setTagCompound(tag);
        spawnAsEntity(world, pos, dropped);
        world.removeTileEntity(pos);
    }

    @Nonnull
    public IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FacingProp.HORIZONTALS, placer.getHorizontalFacing().getOpposite());
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(FacingProp.HORIZONTALS).getHorizontalIndex();
    }

    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FacingProp.HORIZONTALS, EnumFacing.byHorizontalIndex(meta));
    }

    @Nonnull
    @Override
    public IBlockState getActualState(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos) {
        if (world.getTileEntity(pos) instanceof EStorageEnergyCell cell) {
            return state.withProperty(EnergyCellStatus.STATUS, EStorageEnergyCell.getStatusFromFillFactor(cell.getFillFactor()));
        }
        return super.getActualState(state, world, pos);
    }

    @Override
    public void onBlockPlacedBy(@Nonnull final World world,
                                @Nonnull final BlockPos pos,
                                @Nonnull final IBlockState state,
                                @Nonnull final EntityLivingBase placer,
                                @Nonnull final ItemStack stack)
    {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("energyStored") && tag.hasKey("maxEnergyStore")) {
            if (world.getTileEntity(pos) instanceof final EStorageEnergyCell cell) {
                cell.readCustomNBT(tag);
            }
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FacingProp.HORIZONTALS, rot.rotate(state.getValue(FacingProp.HORIZONTALS)));
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FacingProp.HORIZONTALS, EnergyCellStatus.STATUS);
    }

}
