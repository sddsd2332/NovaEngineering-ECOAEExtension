package github.kasuminova.ecoaeextension.common;

import appeng.api.AEApi;
import appeng.api.storage.ICellHandler;
import github.kasuminova.mmce.common.integration.ModIntegrationAE2;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.container.*;
import github.kasuminova.ecoaeextension.common.estorage.EStorageCellHandler;
import github.kasuminova.ecoaeextension.common.handler.ECalculatorEventHandler;
import github.kasuminova.ecoaeextension.common.handler.EFabricatorEventHandler;
import github.kasuminova.ecoaeextension.common.handler.EStorageEventHandler;
import github.kasuminova.ecoaeextension.common.integration.theoneprobe.IntegrationTOP;
import github.kasuminova.ecoaeextension.common.registry.RegistryBlocks;
import github.kasuminova.ecoaeextension.common.registry.RegistryItems;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorPatternBus;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageController;
import github.kasuminova.ecoaeextension.common.util.MachineCoolants;
import github.kasuminova.ecoaeextension.mixin.ae2.AccessorCellRegistry;
import hellfirepvp.modularmachinery.common.base.Mods;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("MethodMayBeStatic")
public class CommonProxy implements IGuiHandler {

    public CommonProxy() {
        MinecraftForge.EVENT_BUS.register(new RegistryBlocks());
        MinecraftForge.EVENT_BUS.register(new RegistryItems());
    }

    public void construction() {

    }

    public void preInit() {
        NetworkRegistry.INSTANCE.registerGuiHandler(ECOAEExtension.MOD_ID, this);

        MinecraftForge.EVENT_BUS.register(EStorageEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(EFabricatorEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ECalculatorEventHandler.INSTANCE);
    }

    public void init() {
        IntegrationTOP.registerProvider();
        if (Mods.AE2.isPresent()) {
            List<ICellHandler> handlers = ((AccessorCellRegistry) (AEApi.instance().registries().cell())).getHandlers();
            handlers.add(0, EStorageCellHandler.INSTANCE);
        }
    }

    public void postInit() {
        MachineCoolants.INSTANCE.init();
    }

    public void loadComplete() {

    }

    @Nullable
    @Override
    public Object getServerGuiElement(final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        GuiType type = GuiType.values()[MathHelper.clamp(ID, 0, GuiType.values().length - 1)];
        Class<? extends TileEntity> required = type.requiredTileEntity;
        TileEntity present = null;
        if (required != null) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te != null && required.isAssignableFrom(te.getClass())) {
                present = te;
            } else {
                return null;
            }
        }

        return switch (type) {
            case ESTORAGE_CONTROLLER -> new ContainerEStorageController((EStorageController) present, player);
            case EFABRICATOR_CONTROLLER -> {
                EFabricatorController efController = (EFabricatorController) present;
                if (efController.getChannel() != null && ModIntegrationAE2.securityCheck(player, efController.getChannel().getProxy())) {
                    yield null;
                }
                yield new ContainerEFabricatorController(efController, player);
            }
            case EFABRICATOR_PATTERN_SEARCH -> {
                EFabricatorController efController = (EFabricatorController) present;
                if (efController.getChannel() != null && ModIntegrationAE2.securityCheck(player, efController.getChannel().getProxy())) {
                    yield null;
                }
                yield new ContainerEFabricatorPatternSearch(efController, player);
            }
            case EFABRICATOR_PATTERN_BUS -> {
                EFabricatorPatternBus efPatternBus = (EFabricatorPatternBus) present;
                EFabricatorController efController = efPatternBus.getController();
                if (efController != null && efController.getChannel() != null && ModIntegrationAE2.securityCheck(player, efController.getChannel().getProxy())) {
                    yield null;
                }
                yield new ContainerEFabricatorPatternBus(efPatternBus, player);
            }
            case ECALCULATOR_CONTROLLER -> {
                ECalculatorController ecController = (ECalculatorController) present;
                if (ecController.getChannel() != null && ModIntegrationAE2.securityCheck(player, ecController.getChannel().getProxy())) {
                    yield null;
                }
                yield new ContainerECalculatorController((ECalculatorController) present, player);
            }
        };
    }

    @Nullable
    @Override
    public Object getClientGuiElement(final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        return null;
    }

    public enum GuiType {

        ESTORAGE_CONTROLLER(EStorageController.class),
        EFABRICATOR_CONTROLLER(EFabricatorController.class),
        EFABRICATOR_PATTERN_SEARCH(EFabricatorController.class),
        EFABRICATOR_PATTERN_BUS(EFabricatorPatternBus.class),
        ECALCULATOR_CONTROLLER(ECalculatorController.class),
        ;

        public final Class<? extends TileEntity> requiredTileEntity;

        GuiType(@Nullable Class<? extends TileEntity> requiredTileEntity) {
            this.requiredTileEntity = requiredTileEntity;
        }
    }
}
