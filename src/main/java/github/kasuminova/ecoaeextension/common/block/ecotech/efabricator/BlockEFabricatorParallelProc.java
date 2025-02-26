package github.kasuminova.ecoaeextension.common.block.ecotech.efabricator;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.prop.FacingProp;
import github.kasuminova.ecoaeextension.common.core.CreativeTabNovaEng;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorParallelProc;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorParallelProc.Modifier;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"deprecation", "ArraysAsListWithZeroOrOneArgument"})
public class BlockEFabricatorParallelProc extends BlockEFabricatorPart {

    public static final BlockEFabricatorParallelProc L4 = new BlockEFabricatorParallelProc("l4", 
            Arrays.asList(
                    new Modifier(EFabricatorParallelProc.Type.ADD, 24, false)
            ),
            Arrays.asList(
                    new Modifier(EFabricatorParallelProc.Type.ADD, 32, false),
                    new Modifier(EFabricatorParallelProc.Type.MULTIPLY, 0.99, true)
            )
    );
    public static final BlockEFabricatorParallelProc L6 = new BlockEFabricatorParallelProc("l6",
            Arrays.asList(
                    new Modifier(EFabricatorParallelProc.Type.ADD, 72, false)
            ),
            Arrays.asList(
                    new Modifier(EFabricatorParallelProc.Type.ADD, 96, false),
                    new Modifier(EFabricatorParallelProc.Type.MULTIPLY, 0.98, true)
            )
    );
    public static final BlockEFabricatorParallelProc L9 = new BlockEFabricatorParallelProc("l9",
            Arrays.asList(
                    new Modifier(EFabricatorParallelProc.Type.ADD, 256, false)
            ),
            Arrays.asList(
                    new Modifier(EFabricatorParallelProc.Type.ADD, 384, false),
                    new Modifier(EFabricatorParallelProc.Type.MULTIPLY, 0.97, true)
            )
    );

    protected final List<Modifier> modifiers;
    protected final List<Modifier> overclockModifiers;

    protected BlockEFabricatorParallelProc(final String level, final List<Modifier> modifiers, final List<Modifier> overclockModifiers) {
        super(Material.IRON);
        this.setHardness(20.0F);
        this.setResistance(2000.0F);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 2);
        this.setCreativeTab(CreativeTabNovaEng.INSTANCE);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FacingProp.HORIZONTALS, EnumFacing.NORTH)
        );
        this.setRegistryName(new ResourceLocation(ECOAEExtension.MOD_ID, "efabricator_parallel_proc_" + level));
        this.setTranslationKey(ECOAEExtension.MOD_ID + '.' + "efabricator_parallel_proc_" + level);
        this.modifiers = modifiers;
        this.overclockModifiers = overclockModifiers;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public List<Modifier> getOverclockModifiers() {
        return overclockModifiers;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull final World world, @Nonnull final IBlockState state) {
        return new EFabricatorParallelProc(modifiers, overclockModifiers);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull final World world, final int meta) {
        return new EFabricatorParallelProc(modifiers, overclockModifiers);
    }

    @Override
    public int getLightValue(@Nonnull final IBlockState state) {
        return 10;
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
        return new BlockStateContainer(this, FacingProp.HORIZONTALS);
    }

}
