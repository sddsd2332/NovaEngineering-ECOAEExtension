package github.kasuminova.ecoaeextension.common.block.ecotech.estorage;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageMEChannel;
import hellfirepvp.modularmachinery.common.CommonProxy;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockEStorageMEChannel extends BlockEStoragePart {

    public static final BlockEStorageMEChannel INSTANCE = new BlockEStorageMEChannel();

    public BlockEStorageMEChannel() {
        super(Material.IRON);
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 1);
        this.setCreativeTab(CommonProxy.creativeTabModularMachinery);
        this.setDefaultState(this.blockState.getBaseState());
        this.setRegistryName(new ResourceLocation(ECOAEExtension.MOD_ID, "estorage_me_channel"));
        this.setTranslationKey(ECOAEExtension.MOD_ID + '.' + "estorage_me_channel");
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull final World world, @Nonnull final IBlockState state) {
        return new EStorageMEChannel();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull final World world, final int meta) {
        return new EStorageMEChannel();
    }

}
