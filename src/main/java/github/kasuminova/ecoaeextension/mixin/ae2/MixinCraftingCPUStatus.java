package github.kasuminova.ecoaeextension.mixin.ae2;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.container.implementations.CraftingCPUStatus;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.Levels;
import github.kasuminova.ecoaeextension.common.ecalculator.ECPUCluster;
import github.kasuminova.ecoaeextension.common.ecalculator.ECPUStatus;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftingCPUStatus.class, remap = false)
public class MixinCraftingCPUStatus implements ECPUStatus {

    @Unique
    private Levels novaeng_ec$ecLevel;

    @Inject(method = "<init>(Lappeng/api/networking/crafting/ICraftingCPU;I)V", at = @At("RETURN"))
    private void injectInit(final ICraftingCPU cluster, final int serial, final CallbackInfo ci) {
        if (cluster instanceof ECPUCluster ecpuCluster) {
            this.novaeng_ec$ecLevel = ecpuCluster.novaeng_ec$getControllerLevel();
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void injectInit(final NBTTagCompound i, final CallbackInfo ci) {
        if (i.hasKey("ecLevel")) {
            this.novaeng_ec$ecLevel = Levels.values()[i.getByte("ecLevel")];
        }
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    private void injectWriteToNBT(final NBTTagCompound i, final CallbackInfo ci) {
        if (novaeng_ec$ecLevel == null) {
            return;
        }
        i.setByte("ecLevel", (byte) novaeng_ec$ecLevel.ordinal());
    }

    @Override
    public Levels novaeng_ec$getLevel() {
        return novaeng_ec$ecLevel;
    }

}
