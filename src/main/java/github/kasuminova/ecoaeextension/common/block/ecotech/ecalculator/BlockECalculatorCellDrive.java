package github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator;

import appeng.tile.inventory.AppEngInternalInventory;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.DriveLink;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.DriveStatus;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.DriveStorageLevel;
import github.kasuminova.ecoaeextension.common.block.prop.FacingProp;
import github.kasuminova.ecoaeextension.common.item.ecalculator.ECalculatorCell;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorCellDrive;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

@SuppressWarnings("deprecation")
public class BlockECalculatorCellDrive extends BlockECalculatorPart {

    public static final BlockECalculatorCellDrive INSTANCE = new BlockECalculatorCellDrive();

    protected BlockECalculatorCellDrive() {
        super(Material.IRON);
        this.setRegistryName(new ResourceLocation(ECOAEExtension.MOD_ID, "ecalculator_cell_drive"));
        this.setTranslationKey(ECOAEExtension.MOD_ID + '.' + "ecalculator_cell_drive");
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FacingProp.HORIZONTALS, EnumFacing.NORTH)
                .withProperty(DriveLink.LINK, DriveLink.NONE)
                .withProperty(DriveStatus.STATUS, DriveStatus.OFF)
                .withProperty(DriveStorageLevel.STORAGE_LEVEL, DriveStorageLevel.EMPTY)
        );
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull final World worldIn, final int meta) {
        return new ECalculatorCellDrive();
    }

    @Nonnull
    @Override
    public IBlockState getActualState(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof ECalculatorCellDrive drive)) {
            return state;
        }

        AppEngInternalInventory driveInv = drive.getDriveInv();
        ItemStack stack = driveInv.getStackInSlot(0);
        if (stack.isEmpty() || !(stack.getItem() instanceof ECalculatorCell cell)) {
            return state;
        }

        IBlockState newState = state;
        if (drive.getControllerLevel() != null) {
            newState = newState.withProperty(DriveStatus.STATUS, DriveStatus.ON);
        }

        EnumFacing side = drive.getConnectedSide();
        if (side != null) {
            switch (side) {
                case UP -> newState = newState.withProperty(DriveLink.LINK, DriveLink.UP);
                case DOWN -> newState = newState.withProperty(DriveLink.LINK, DriveLink.DOWN);
            }
        }
        return newState.withProperty(DriveStorageLevel.STORAGE_LEVEL, cell.getLevel());
    }

    @Override
    public void breakBlock(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof ECalculatorCellDrive drive) {
            AppEngInternalInventory inv = drive.getDriveInv();
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    spawnAsEntity(worldIn, pos, stack);
                    inv.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public int getLightValue(@Nonnull final IBlockState state) {
        if (state.getValue(DriveStatus.STATUS) == DriveStatus.ON && state.getValue(DriveStorageLevel.STORAGE_LEVEL) != DriveStorageLevel.EMPTY) {
            return 12;
        }
        return state.getValue(DriveStorageLevel.STORAGE_LEVEL) == DriveStorageLevel.EMPTY ? 4 : 8;
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return getDefaultState().withProperty(FacingProp.HORIZONTALS, EnumFacing.byHorizontalIndex(meta));
    }

    @Override
    public int getMetaFromState(@Nonnull final IBlockState state) {
        return state.getValue(FacingProp.HORIZONTALS).getHorizontalIndex();
    }

    @Nonnull
    public IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FacingProp.HORIZONTALS, placer.getHorizontalFacing().getOpposite());
    }

    @Nonnull
    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FacingProp.HORIZONTALS, rot.rotate(state.getValue(FacingProp.HORIZONTALS)));
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FacingProp.HORIZONTALS, DriveLink.LINK, DriveStatus.STATUS, DriveStorageLevel.STORAGE_LEVEL);
    }

}
