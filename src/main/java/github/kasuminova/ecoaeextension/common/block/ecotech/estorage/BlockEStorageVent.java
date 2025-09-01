package github.kasuminova.ecoaeextension.common.block.ecotech.estorage;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.prop.FacingProp;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

@SuppressWarnings("deprecation")
public class BlockEStorageVent extends BlockEStorage {

    public static final BlockEStorageVent INSTANCE = new BlockEStorageVent();

    protected BlockEStorageVent() {
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FacingProp.HORIZONTALS, EnumFacing.NORTH)
        );
        this.setRegistryName(new ResourceLocation(ECOAEExtension.MOD_ID, "estorage_vent"));
        this.setTranslationKey(ECOAEExtension.MOD_ID + '.' + "estorage_vent");
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
        // 鉴于某人把模型做反了，所以这里不反向。
        EnumFacing placerFacing = placer.getHorizontalFacing();
        return this.getDefaultState().withProperty(FacingProp.HORIZONTALS, placerFacing);
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
        return new BlockStateContainer(this, FacingProp.HORIZONTALS);
    }

}
