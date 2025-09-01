package github.kasuminova.ecoaeextension.mixin.mmce;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.BlockECalculatorController;
import github.kasuminova.ecoaeextension.common.block.ecotech.efabricator.BlockEFabricatorController;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.BlockEStorageController;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;


@Mixin(MachineRegistry.class)
public class MixinMachineRegistry {

    @Redirect(
            method = "getWaitForLoadMachines",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    remap = false
            ),
            remap = false
    )
    @SuppressWarnings("SameReturnValue")
    private static boolean filterSpecialMachine(final List<Object> instance, final Object e) {
        DynamicMachine machine = (DynamicMachine) e;
        ResourceLocation registryName = machine.getRegistryName();
        ResourceLocation novaEngResLoc = new ResourceLocation(ECOAEExtension.MOD_ID, registryName.getPath());
        if (BlockEStorageController.REGISTRY.containsKey(novaEngResLoc)) {
            BlockController.MACHINE_CONTROLLERS.put(machine, BlockEStorageController.REGISTRY.get(novaEngResLoc));
            return true;
        }
        if (BlockEFabricatorController.REGISTRY.containsKey(novaEngResLoc)) {
            BlockController.MACHINE_CONTROLLERS.put(machine, BlockEFabricatorController.REGISTRY.get(novaEngResLoc));
            return true;
        }
        if (BlockECalculatorController.REGISTRY.containsKey(novaEngResLoc)) {
            BlockController.MACHINE_CONTROLLERS.put(machine, BlockECalculatorController.REGISTRY.get(novaEngResLoc));
            return true;
        }
        instance.add(e);
        return true;
    }

}
