package github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import github.kasuminova.mmce.common.util.Sides;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.Levels;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.ThreadCoreStatus;
import github.kasuminova.ecoaeextension.common.block.prop.FacingProp;
import github.kasuminova.ecoaeextension.common.item.ecalculator.ItemECalculatorThreadCore;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorThreadCore;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("deprecation")
public class BlockECalculatorThreadCore extends BlockECalculatorPart {

    public static final BlockECalculatorThreadCore L4 = new BlockECalculatorThreadCore("l4", 1, 0);
    public static final BlockECalculatorThreadCore L6 = new BlockECalculatorThreadCore("l6", 2, 0);
    public static final BlockECalculatorThreadCore L9 = new BlockECalculatorThreadCore("l9", 4, 0);

    protected final int threads;
    protected final int hyperThreads;
    protected ItemECalculatorThreadCore item = null;

    protected BlockECalculatorThreadCore(final ResourceLocation registryName, final String translationKey, final int threads, final int hyperThreads) {
        super(Material.IRON);
        this.threads = threads;
        this.hyperThreads = hyperThreads;
        this.setRegistryName(registryName);
        this.setTranslationKey(translationKey);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FacingProp.HORIZONTALS, EnumFacing.NORTH)
                .withProperty(ThreadCoreStatus.STATUS, ThreadCoreStatus.OFF)
        );
    }

    public ItemECalculatorThreadCore getItem() {
        return item;
    }

    public BlockECalculatorThreadCore setItem(final ItemECalculatorThreadCore item) {
        this.item = item;
        return this;
    }

    public int getThreads() {
        return threads;
    }

    public int getHyperThreads() {
        return hyperThreads;
    }

    protected BlockECalculatorThreadCore(final String level, final int threads, final int hyperThreads) {
        this(
                new ResourceLocation(ECOAEExtension.MOD_ID, "ecalculator_thread_core_" + level),
                ECOAEExtension.MOD_ID + '.' + "ecalculator_thread_core_" + level,
                threads, hyperThreads
        );
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull final World worldIn, final int meta) {
        return new ECalculatorThreadCore(this.threads, this.hyperThreads);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull final World world, @Nonnull final IBlockState state) {
        return new ECalculatorThreadCore(this.threads, this.hyperThreads);
    }

    @Override
    public void dropBlockAsItemWithChance(@Nonnull final World worldIn, @Nonnull final BlockPos pos, @Nonnull final IBlockState state, final float chance, final int fortune) {
    }

    @Override
    public void getDrops(@Nonnull final NonNullList<ItemStack> drops, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state, final int fortune) {
    }

    @Override
    public void breakBlock(@Nonnull final World worldIn, @Nonnull final BlockPos pos, @Nonnull final IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        ItemStack dropped = new ItemStack(item);

        if (te == null || Sides.isRunningOnClient()) {
            spawnAsEntity(worldIn, pos, dropped);
            worldIn.removeTileEntity(pos);
            return;
        }
        if (!(te instanceof final ECalculatorThreadCore threadCore)) {
            spawnAsEntity(worldIn, pos, dropped);
            worldIn.removeTileEntity(pos);
            return;
        }
        final List<CraftingCPUCluster> cpus = threadCore.getCpus();
        if (cpus.isEmpty()) {
            spawnAsEntity(worldIn, pos, dropped);
            worldIn.removeTileEntity(pos);
            return;
        }

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final NBTTagCompound tag = new NBTTagCompound();
        threadCore.writeCPUNBT(tag);

        try {
            CompressedStreamTools.writeCompressed(tag, bos);
        } catch (IOException e) {
            ECOAEExtension.log.error("Failed to write CPU NBT to byte array!", e);
            spawnAsEntity(worldIn, pos, dropped);
            worldIn.removeTileEntity(pos);
            return;
        } finally {
            cpus.clear();
        }

        NBTTagCompound itemTag = new NBTTagCompound();
        itemTag.setByteArray("compressedCpuNBT", bos.toByteArray());
        dropped.setTagCompound(itemTag);
        threadCore.onBlockDestroyed();

        try {
            bos.close();
        } catch (IOException e) {
            ECOAEExtension.log.error("Failed to close byte array streams!", e);
        }

        spawnAsEntity(worldIn, pos, dropped);
        worldIn.removeTileEntity(pos);
    }

    @Override
    public void onBlockPlacedBy(@Nonnull final World worldIn,
                                @Nonnull final BlockPos pos,
                                @Nonnull final IBlockState state,
                                @Nonnull final EntityLivingBase placer,
                                @Nonnull final ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        TileEntity te = worldIn.getTileEntity(pos);
        NBTTagCompound tag = stack.getTagCompound();
        if (te instanceof final ECalculatorThreadCore threadCore && tag != null && tag.hasKey("compressedCpuNBT")) {
            byte[] cpuNBTBytes = tag.getByteArray("compressedCpuNBT");
            if (cpuNBTBytes.length == 0) {
                return;
            }
            try (ByteArrayInputStream bis = new ByteArrayInputStream(cpuNBTBytes)) {
                NBTTagCompound cpuNBT = CompressedStreamTools.readCompressed(bis);
                threadCore.readCPUNBT(cpuNBT);
            } catch (IOException e) {
                ECOAEExtension.log.error("Failed to read CPU NBT from byte array!", e);
            }
        }
    }

    @Nonnull
    @Override
    public IBlockState getActualState(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof ECalculatorThreadCore threadCore)) {
            return state;
        }

        Levels level = threadCore.getControllerLevel();
        if (level == null) {
            return state;
        }

        return state.withProperty(ThreadCoreStatus.STATUS, threadCore.getThreads() > 0 ? ThreadCoreStatus.RUN : ThreadCoreStatus.ON);
    }

    @Override
    public int getLightValue(@Nonnull final IBlockState state) {
        if (state.getValue(ThreadCoreStatus.STATUS) == ThreadCoreStatus.RUN) {
            return 15;
        }
        if (state.getValue(ThreadCoreStatus.STATUS) == ThreadCoreStatus.ON) {
            return 10;
        }
        return 5;
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
        return new BlockStateContainer(this, FacingProp.HORIZONTALS, ThreadCoreStatus.STATUS);
    }

}
