package github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.TransmitterBusLink;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.TransmitterBusLinkLevel;
import github.kasuminova.ecoaeextension.common.block.prop.FacingProp;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorTransmitterBus;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
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
public class BlockECalculatorTransmitterBus extends BlockECalculatorPart {

    public static final BlockECalculatorTransmitterBus INSTANCE = new BlockECalculatorTransmitterBus();

    protected BlockECalculatorTransmitterBus() {
        super(Material.IRON);
        this.setRegistryName(new ResourceLocation(ECOAEExtension.MOD_ID, "ecalculator_transmitter_bus"));
        this.setTranslationKey(ECOAEExtension.MOD_ID + '.' + "ecalculator_transmitter_bus");
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FacingProp.HORIZONTALS, EnumFacing.NORTH)
                .withProperty(TransmitterBusLink.LINK, TransmitterBusLink.NONE)
                .withProperty(TransmitterBusLinkLevel.LINK_LEVEL, TransmitterBusLinkLevel.NONE)
        );
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull final World worldIn, final int meta) {
        return new ECalculatorTransmitterBus();
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull final World world, @Nonnull final IBlockState state) {
        return new ECalculatorTransmitterBus();
    }

    @Override
    public void neighborChanged(@Nonnull final IBlockState state, @Nonnull final World worldIn, @Nonnull final BlockPos pos, @Nonnull final Block blockIn, @Nonnull final BlockPos fromPos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof ECalculatorTransmitterBus bus) {
            bus.neighborChanged(fromPos);
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Nonnull
    @Override
    public IBlockState getActualState(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof ECalculatorTransmitterBus bus)) {
            return state;
        }

        final IBlockState newState = state.withProperty(TransmitterBusLinkLevel.LINK_LEVEL, bus.getLinkLevel());
        if (bus.isAllConnected()) {
            return newState.withProperty(TransmitterBusLink.LINK, TransmitterBusLink.ALL);
        } else if (bus.isUpConnected()) {
            return newState.withProperty(TransmitterBusLink.LINK, TransmitterBusLink.UP);
        } else if (bus.isDownConnected()) {
            return newState.withProperty(TransmitterBusLink.LINK, TransmitterBusLink.DOWN);
        }

        return newState;
    }

    @Override
    public int getLightValue(@Nonnull final IBlockState state) {
        TransmitterBusLink link = state.getValue(TransmitterBusLink.LINK);
        if (link == TransmitterBusLink.ALL) {
            return 12;
        }
        if (link == TransmitterBusLink.UP || link == TransmitterBusLink.DOWN) {
            return 8;
        }
        return 4;
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
        return new BlockStateContainer(this, FacingProp.HORIZONTALS, TransmitterBusLink.LINK, TransmitterBusLinkLevel.LINK_LEVEL);
    }

}
