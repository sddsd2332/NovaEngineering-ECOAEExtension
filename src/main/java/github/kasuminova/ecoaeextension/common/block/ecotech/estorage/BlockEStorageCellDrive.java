package github.kasuminova.ecoaeextension.common.block.ecotech.estorage;

import appeng.api.AEApi;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.tile.inventory.AppEngCellInventory;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStatus;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageCapacity;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageLevel;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageType;
import github.kasuminova.ecoaeextension.common.block.prop.FacingProp;
import github.kasuminova.ecoaeextension.common.core.CreativeTabNovaEng;
import github.kasuminova.ecoaeextension.common.estorage.EStorageCellHandler;
import github.kasuminova.ecoaeextension.common.item.estorage.EStorageCell;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageCellDrive;
import net.minecraft.block.SoundType;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

@SuppressWarnings("deprecation")
public class BlockEStorageCellDrive extends BlockEStoragePart {

    public static final BlockEStorageCellDrive INSTANCE = new BlockEStorageCellDrive();

    protected BlockEStorageCellDrive() {
        super(Material.IRON);
        this.setHardness(20.0F);
        this.setResistance(2000.0F);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 2);
        this.setCreativeTab(CreativeTabNovaEng.INSTANCE);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FacingProp.HORIZONTALS, EnumFacing.NORTH)
                .withProperty(DriveStorageType.STORAGE_TYPE, DriveStorageType.EMPTY)
                .withProperty(DriveStorageLevel.STORAGE_LEVEL, DriveStorageLevel.EMPTY)
                .withProperty(DriveStorageCapacity.STORAGE_CAPACITY, DriveStorageCapacity.EMPTY)
                .withProperty(DriveStatus.STATUS, DriveStatus.IDLE)
        );
        this.setRegistryName(new ResourceLocation(ECOAEExtension.MOD_ID, "estorage_cell_drive"));
        this.setTranslationKey(ECOAEExtension.MOD_ID + '.' + "estorage_cell_drive");
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull final World world, @Nonnull final IBlockState state) {
        return new EStorageCellDrive();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull final World world, final int meta) {
        return new EStorageCellDrive();
    }

    @Override
    public void breakBlock(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof EStorageCellDrive drive) {
            AppEngCellInventory inv = drive.getDriveInv();
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
        return state.getValue(DriveStorageType.STORAGE_TYPE) == DriveStorageType.EMPTY ? 2 : 6;
    }

    @Nonnull
    @Override
    @SuppressWarnings("rawtypes")
    public IBlockState getActualState(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof EStorageCellDrive drive)) {
            return state;
        }
        AppEngCellInventory driveInv = drive.getDriveInv();
        ItemStack stack = driveInv.getStackInSlot(0);
        if (stack.isEmpty()) {
            return state;
        }

        EStorageCellHandler handler = EStorageCellHandler.getHandler(stack);
        if (handler == null) {
            return state;
        }

        EStorageCell<?> cell = (EStorageCell<?>) stack.getItem();
        DriveStorageLevel level = cell.getLevel();
        DriveStorageType type = EStorageCellDrive.getCellType(cell);
        if (type == null) {
            return state;
        }

        final Collection<IStorageChannel<? extends IAEStack<?>>> storageChannels = AEApi.instance().storage().storageChannels();
        ICellInventoryHandler cellInventory = null;
        for (final IStorageChannel<? extends IAEStack<?>> channel : storageChannels) {
            cellInventory = handler.getCellInventory(stack, drive, channel);
            if (cellInventory != null) {
                break;
            }
        }

        if (cellInventory == null) {
            return state;
        }

        return state.withProperty(DriveStorageLevel.STORAGE_LEVEL, level)
                .withProperty(DriveStorageType.STORAGE_TYPE, type)
                .withProperty(DriveStatus.STATUS, drive.isWriting() ? DriveStatus.RUN : DriveStatus.IDLE)
                .withProperty(DriveStorageCapacity.STORAGE_CAPACITY, EStorageCellDrive.getCapacity(cellInventory));
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
    @SuppressWarnings("deprecation")
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FacingProp.HORIZONTALS, rot.rotate(state.getValue(FacingProp.HORIZONTALS)));
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this,
                FacingProp.HORIZONTALS,
                DriveStorageType.STORAGE_TYPE,
                DriveStorageLevel.STORAGE_LEVEL,
                DriveStorageCapacity.STORAGE_CAPACITY,
                DriveStatus.STATUS
        );
    }

}
