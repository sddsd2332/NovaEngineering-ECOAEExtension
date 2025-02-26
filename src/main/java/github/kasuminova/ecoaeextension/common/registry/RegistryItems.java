package github.kasuminova.ecoaeextension.common.registry;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.client.renderer.EStorageEnergyCellItemRenderer;
import github.kasuminova.ecoaeextension.common.item.ecalculator.ECalculatorCell;
import github.kasuminova.ecoaeextension.common.item.estorage.EStorageCellFluid;
import github.kasuminova.ecoaeextension.common.item.estorage.EStorageCellItem;
import github.kasuminova.ecoaeextension.common.item.estorage.ItemBlockEStorageEnergyCell;
import hellfirepvp.modularmachinery.common.item.ItemDynamicColor;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

import static hellfirepvp.modularmachinery.common.registry.RegistryItems.pendingDynamicColorItems;

@SuppressWarnings({"MethodMayBeStatic", "UnusedReturnValue"})
public class RegistryItems {
    public static final List<Item> ITEMS_TO_REGISTER = new LinkedList<>();
    public static final List<Item> ITEMS_TO_REGISTER_CT = new LinkedList<>();
    public static final Map<String, Item> CUSTOM_MODEL_ITEMS_TO_REGISTER_CT = new LinkedHashMap<>();

    public static final List<Item> ITEM_MODELS_TO_REGISTER = new LinkedList<>();
    public static final Map<String, Item> ITEM_CUSTOM_MODELS_TO_REGISTER = new LinkedHashMap<>();

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        GenericRegistryPrimer.INSTANCE.wipe(event.getGenericType());

        ITEMS_TO_REGISTER.add(EStorageCellItem.LEVEL_A);
        ITEMS_TO_REGISTER.add(EStorageCellItem.LEVEL_B);
        ITEMS_TO_REGISTER.add(EStorageCellItem.LEVEL_C);
        ITEMS_TO_REGISTER.add(EStorageCellFluid.LEVEL_A);
        ITEMS_TO_REGISTER.add(EStorageCellFluid.LEVEL_B);
        ITEMS_TO_REGISTER.add(EStorageCellFluid.LEVEL_C);

        ITEMS_TO_REGISTER.add(ECalculatorCell.L4);
        ITEMS_TO_REGISTER.add(ECalculatorCell.L6);
        ITEMS_TO_REGISTER.add(ECalculatorCell.L9);

        registerItems();

        GenericRegistryPrimer.INSTANCE.fillRegistry(event.getRegistry().getRegistrySuperType(), event.getRegistry());
    }

    public static void registerItems() {
        ITEMS_TO_REGISTER.forEach(RegistryItems::registerItem);
        ITEMS_TO_REGISTER.clear();
        ITEMS_TO_REGISTER_CT.forEach(RegistryItems::registerItem);
        ITEMS_TO_REGISTER_CT.clear();
        CUSTOM_MODEL_ITEMS_TO_REGISTER_CT.forEach((path, item) -> registerItem(item, path));
        CUSTOM_MODEL_ITEMS_TO_REGISTER_CT.clear();
    }

    public static void registerItemModels() {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            ITEM_MODELS_TO_REGISTER.clear();
            ITEM_CUSTOM_MODELS_TO_REGISTER.clear();
            return;
        }
        ITEM_MODELS_TO_REGISTER.forEach(RegistryItems::registerItemModel);
        ITEM_MODELS_TO_REGISTER.clear();
        ITEM_CUSTOM_MODELS_TO_REGISTER.forEach((path, item) -> registerItemModel(item, path));
        ITEM_CUSTOM_MODELS_TO_REGISTER.clear();
        setMeshDef();
    }

    @SideOnly(Side.CLIENT)
    private static void setMeshDef() {
        ModelLoader.setCustomMeshDefinition(ItemBlockEStorageEnergyCell.L4, new EStorageEnergyCellItemRenderer(ItemBlockEStorageEnergyCell.L4, "l4"));
        ModelLoader.setCustomMeshDefinition(ItemBlockEStorageEnergyCell.L6, new EStorageEnergyCellItemRenderer(ItemBlockEStorageEnergyCell.L6, "l6"));
        ModelLoader.setCustomMeshDefinition(ItemBlockEStorageEnergyCell.L9, new EStorageEnergyCellItemRenderer(ItemBlockEStorageEnergyCell.L9, "l9"));
    }

    public static <T extends Item> T registerItem(T item) {
        ITEM_MODELS_TO_REGISTER.add(item);
        GenericRegistryPrimer.INSTANCE.register(item);
        if (item instanceof ItemDynamicColor) {
            pendingDynamicColorItems.add((ItemDynamicColor) item);
        }
        return item;
    }

    public static <T extends Item> T registerItem(T item, String modelPath) {
        ITEM_CUSTOM_MODELS_TO_REGISTER.put(modelPath, item);
        GenericRegistryPrimer.INSTANCE.register(item);
        if (item instanceof ItemDynamicColor) {
            pendingDynamicColorItems.add((ItemDynamicColor) item);
        }
        return item;
    }

    public static void registerItemModel(final Item item) {
        NonNullList<ItemStack> list = NonNullList.create();
        ResourceLocation registryName = Objects.requireNonNull(item.getRegistryName());

        item.getSubItems(Objects.requireNonNull(item.getCreativeTab()), list);
        if (list.isEmpty()) {
            ModelLoader.setCustomModelResourceLocation(
                    item, 0, new ModelResourceLocation(registryName, "inventory"));
        } else {
            list.forEach(stack -> ModelLoader.setCustomModelResourceLocation(
                    item, stack.getItemDamage(), new ModelResourceLocation(registryName, "inventory")));
        }

        ECOAEExtension.log.debug("REGISTERED ITEM MODEL: {}", registryName);
    }

    public static void registerItemModel(final Item item, final String modelPath) {
        NonNullList<ItemStack> list = NonNullList.create();
        ResourceLocation registryName = Objects.requireNonNull(item.getRegistryName());
        ResourceLocation modelLocation = new ResourceLocation(registryName.getNamespace(), modelPath);

        item.getSubItems(Objects.requireNonNull(item.getCreativeTab()), list);
        if (list.isEmpty()) {
            ModelLoader.setCustomModelResourceLocation(
                    item, 0, new ModelResourceLocation(modelLocation, "inventory"));
        } else {
            list.forEach(stack -> ModelLoader.setCustomModelResourceLocation(
                    item, stack.getItemDamage(), new ModelResourceLocation(modelLocation, "inventory")));
        }

        ECOAEExtension.log.debug("REGISTERED ITEM MODEL: {}", modelLocation);
    }
}
