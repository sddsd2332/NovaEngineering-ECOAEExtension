package github.kasuminova.ecoaeextension.common.registry;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.*;
import github.kasuminova.ecoaeextension.common.block.ecotech.efabricator.*;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.*;
import github.kasuminova.ecoaeextension.common.item.ItemBlockME;
import github.kasuminova.ecoaeextension.common.item.ecalculator.*;
import github.kasuminova.ecoaeextension.common.item.efabriactor.*;
import github.kasuminova.ecoaeextension.common.item.estorage.ItemEStorageController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.*;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.*;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageCellDrive;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageEnergyCell;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageMEChannel;
import hellfirepvp.modularmachinery.common.block.BlockCustomName;
import hellfirepvp.modularmachinery.common.block.BlockDynamicColor;
import hellfirepvp.modularmachinery.common.block.BlockMachineComponent;
import hellfirepvp.modularmachinery.common.item.ItemBlockCustomName;
import hellfirepvp.modularmachinery.common.item.ItemBlockMachineComponent;
import hellfirepvp.modularmachinery.common.item.ItemBlockMachineComponentCustomName;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static hellfirepvp.modularmachinery.common.registry.RegistryBlocks.pendingIBlockColorBlocks;

@SuppressWarnings({"MethodMayBeStatic", "UnusedReturnValue"})
public class RegistryBlocks {
    public static final List<Block> BLOCK_MODEL_TO_REGISTER = new ArrayList<>();

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        GenericRegistryPrimer.INSTANCE.wipe(event.getGenericType());

        registerBlocks();
        registerTileEntities();

        GenericRegistryPrimer.INSTANCE.fillRegistry(event.getRegistry().getRegistrySuperType(), event.getRegistry());
    }

    public static void registerBlocks() {
        // EStorage
        prepareItemBlockRegister(new ItemEStorageController(registerBlock(BlockEStorageController.L4)));
        prepareItemBlockRegister(new ItemEStorageController(registerBlock(BlockEStorageController.L6)));
        prepareItemBlockRegister(new ItemEStorageController(registerBlock(BlockEStorageController.L9)));
        prepareItemBlockRegister(registerBlock(BlockEStorageEnergyCell.L4));
        prepareItemBlockRegister(registerBlock(BlockEStorageEnergyCell.L6));
        prepareItemBlockRegister(registerBlock(BlockEStorageEnergyCell.L9));
        prepareItemBlockRegister(registerBlock(BlockEStorageCellDrive.INSTANCE));
        prepareItemBlockRegister(new ItemBlockME(registerBlock(BlockEStorageMEChannel.INSTANCE)));
        prepareItemBlockRegister(registerBlock(BlockEStorageVent.INSTANCE));
        prepareItemBlockRegister(registerBlock(BlockEStorageCasing.INSTANCE));

        // EFabricator
        prepareItemBlockRegister(new ItemEFabricatorController(registerBlock(BlockEFabricatorController.L4)));
        prepareItemBlockRegister(new ItemEFabricatorController(registerBlock(BlockEFabricatorController.L6)));
        prepareItemBlockRegister(new ItemEFabricatorController(registerBlock(BlockEFabricatorController.L9)));
        prepareItemBlockRegister(new ItemEFabricatorParallelProc(registerBlock(BlockEFabricatorParallelProc.L4)));
        prepareItemBlockRegister(new ItemEFabricatorParallelProc(registerBlock(BlockEFabricatorParallelProc.L6)));
        prepareItemBlockRegister(new ItemEFabricatorParallelProc(registerBlock(BlockEFabricatorParallelProc.L9)));
        prepareItemBlockRegister(new ItemEFabricatorMEChannel(registerBlock(BlockEFabricatorMEChannel.INSTANCE)));
        // 烂了，模型作者自己都修不好。
//        prepareItemBlockRegister(registerBlock(BlockEFabricatorTail.L4));
//        prepareItemBlockRegister(registerBlock(BlockEFabricatorTail.L6));
//        prepareItemBlockRegister(registerBlock(BlockEFabricatorTail.L9));
        prepareItemBlockRegister(new ItemEFabricatorPatternBus(registerBlock(BlockEFabricatorPatternBus.INSTANCE)));
        prepareItemBlockRegister(new ItemEFabricatorWorker(registerBlock(BlockEFabricatorWorker.INSTANCE)));
        prepareItemBlockRegister(registerBlock(BlockEFabricatorVent.INSTANCE));
        prepareItemBlockRegister(registerBlock(BlockEFabricatorCasing.INSTANCE));

        // ECalculator
        prepareItemBlockRegister(new ItemECalculatorController(registerBlock(BlockECalculatorController.L4)));
        prepareItemBlockRegister(new ItemECalculatorController(registerBlock(BlockECalculatorController.L6)));
        prepareItemBlockRegister(new ItemECalculatorController(registerBlock(BlockECalculatorController.L9)));
        prepareItemBlockRegister(new ItemECalculatorParallelProc(registerBlock(BlockECalculatorParallelProc.L4)));
        prepareItemBlockRegister(new ItemECalculatorParallelProc(registerBlock(BlockECalculatorParallelProc.L6)));
        prepareItemBlockRegister(new ItemECalculatorParallelProc(registerBlock(BlockECalculatorParallelProc.L9)));
        prepareItemBlockRegister(new ItemECalculatorThreadCore(registerBlock(BlockECalculatorThreadCore.L4)));
        prepareItemBlockRegister(new ItemECalculatorThreadCore(registerBlock(BlockECalculatorThreadCore.L6)));
        prepareItemBlockRegister(new ItemECalculatorThreadCore(registerBlock(BlockECalculatorThreadCore.L9)));
        prepareItemBlockRegister(new ItemECalculatorThreadCore(registerBlock(BlockECalculatorThreadCoreHyper.L4)));
        prepareItemBlockRegister(new ItemECalculatorThreadCore(registerBlock(BlockECalculatorThreadCoreHyper.L6)));
        prepareItemBlockRegister(new ItemECalculatorThreadCore(registerBlock(BlockECalculatorThreadCoreHyper.L9)));
        prepareItemBlockRegister(registerBlock(BlockECalculatorTail.L4));
        prepareItemBlockRegister(registerBlock(BlockECalculatorTail.L6));
        prepareItemBlockRegister(registerBlock(BlockECalculatorTail.L9));
        prepareItemBlockRegister(new ItemECalculatorMEChannel(registerBlock(BlockECalculatorMEChannel.INSTANCE)));
        prepareItemBlockRegister(new ItemECalculatorCellDrive(registerBlock(BlockECalculatorCellDrive.INSTANCE)));
        prepareItemBlockRegister(registerBlock(BlockECalculatorTransmitterBus.INSTANCE));
        prepareItemBlockRegister(registerBlock(BlockECalculatorCasing.INSTANCE));
    }

    public static void registerTileEntities() {
        // EStorage
        registerTileEntity(EStorageController.class, "estorage_controller");
        registerTileEntity(EStorageEnergyCell.class, "estorage_energy_cell");
        registerTileEntity(EStorageCellDrive.class, "estorage_cell_drive");
        registerTileEntity(EStorageMEChannel.class, "estorage_me_channel");

        // EFabricator
        registerTileEntity(EFabricatorController.class, "efabricator_controller");
        registerTileEntity(EFabricatorParallelProc.class, "efabricator_parallel_proc");
        registerTileEntity(EFabricatorTail.class, "efabricator_tail");
        registerTileEntity(EFabricatorPatternBus.class, "efabricator_pattern_bus");
        registerTileEntity(EFabricatorWorker.class, "efabricator_worker");
        registerTileEntity(EFabricatorMEChannel.class, "efabricator_me_channel");

        // ECalculator
        registerTileEntity(ECalculatorController.class, "ecalculator_controller");
        registerTileEntity(ECalculatorParallelProc.class, "ecalculator_parallel_proc");
        registerTileEntity(ECalculatorThreadCore.class, "ecalculator_thread_core");
        registerTileEntity(ECalculatorTail.class, "ecalculator_tail");
        registerTileEntity(ECalculatorMEChannel.class, "ecalculator_me_channel");
        registerTileEntity(ECalculatorCellDrive.class, "ecalculator_cell_drive");
        registerTileEntity(ECalculatorTransmitterBus.class, "ecalculator_transmitter_bus");
    }

    public static void registerBlockModels() {
        BLOCK_MODEL_TO_REGISTER.forEach(RegistryBlocks::registerBlockModel);
        BLOCK_MODEL_TO_REGISTER.clear();
    }

    public static void registerTileEntity(Class<? extends TileEntity> tile, String name) {
        GameRegistry.registerTileEntity(tile, new ResourceLocation(ECOAEExtension.MOD_ID, name));
    }

    public static <T extends Block> T registerBlock(T block) {
        BLOCK_MODEL_TO_REGISTER.add(block);
        GenericRegistryPrimer.INSTANCE.register(block);
        if (block instanceof BlockDynamicColor) {
            pendingIBlockColorBlocks.add((BlockDynamicColor) block);
        }

        return block;
    }

    public static ItemBlock prepareItemBlockRegister(Block block) {
        if (block instanceof BlockMachineComponent) {
            if (block instanceof BlockCustomName) {
                return prepareItemBlockRegister(new ItemBlockMachineComponentCustomName(block));
            } else {
                return prepareItemBlockRegister(new ItemBlockMachineComponent(block));
            }
        } else {
            if (block instanceof BlockCustomName) {
                return prepareItemBlockRegister(new ItemBlockCustomName(block));
            } else {
                return prepareItemBlockRegister(new ItemBlock(block));
            }
        }
    }

    public static <T extends ItemBlock> T prepareItemBlockRegister(T item) {
        Block block = item.getBlock();
        ResourceLocation registryName = Objects.requireNonNull(block.getRegistryName());
        String translationKey = block.getTranslationKey();

        item.setRegistryName(registryName).setTranslationKey(translationKey);
        RegistryItems.ITEMS_TO_REGISTER.add(item);
        return item;
    }

    public static void registerBlockModel(final Block block) {
        Item item = Item.getItemFromBlock(block);
        ResourceLocation registryName = Objects.requireNonNull(item.getRegistryName());
        ModelBakery.registerItemVariants(item, registryName);
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(registryName, "inventory"));
    }
}
