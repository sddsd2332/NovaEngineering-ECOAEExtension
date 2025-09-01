package github.kasuminova.ecoaeextension.mixin.ae2;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.crafting.MECraftingInventory;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import com.glodblock.github.util.FluidCraftingPatternDetails;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import github.kasuminova.ecoaeextension.common.tile.MEPatternProviderNova;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorMEChannel;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorWorker;
import github.kasuminova.ecoaeextension.common.util.MediumType;
import github.kasuminova.mmce.common.tile.MEPatternProvider;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public abstract class MixinCraftingCPUClusterTwo {

    @Shadow protected abstract void postChange(IAEItemStack diff, IActionSource src);

    @Shadow protected abstract void postCraftingStatusChange(IAEItemStack diff);

    @Shadow
    private int remainingOperations;
    @Shadow
    private MachineSource machineSrc;
    @Shadow
    private MECraftingInventory inventory;

    @Unique
    private MediumType r$MediumType = MediumType.NULL;

    @Unique
    private boolean r$IgnoreParallel = false;
    @Unique
    private ICraftingPatternDetails r$pattern;

    @Unique
    private long r$craftingFrequency = 0;

    @WrapOperation(method = "executeCrafting",at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getKey()Ljava/lang/Object;"))
    private Object getKeyR(Map.Entry<ICraftingPatternDetails,AccessorTaskProgress> instance, Operation<ICraftingPatternDetails> original) {
        var key = original.call(instance);

        long max = 0;
        var list = (key.isCraftable() || key instanceof FluidCraftingPatternDetails) ? key.getCondensedOutputs() : key.getCondensedInputs();
        for (IAEItemStack stack : list) {
            long size = stack.getStackSize();
            if (size > max) max = size;
        }

        this.r$craftingFrequency = instance.getValue().getValue();
        if (max * this.r$craftingFrequency > Integer.MAX_VALUE){
            this.r$craftingFrequency = Integer.MAX_VALUE / max;
        }

        return r$pattern = key;
    }

    @Inject(method = "executeCrafting",at = @At(value = "INVOKE", target = "Lappeng/api/networking/crafting/ICraftingMedium;isBusy()Z",shift = At.Shift.AFTER))
    private void executeCraftingI(IEnergyGrid eg, CraftingGridCache cc, CallbackInfo ci, @Local(name = "m") ICraftingMedium instance) {
        if (instance instanceof MEPatternProviderNova mep){
            if (mep.getWorkMode() == MEPatternProvider.WorkModeSetting.DEFAULT
                    || mep.getWorkMode() == MEPatternProvider.WorkModeSetting.ENHANCED_BLOCKING_MODE) {

                for (IAEItemStack input : r$pattern.getCondensedInputs()) {
                    long size = input.getStackSize() * this.r$craftingFrequency;
                    var item = this.inventory.extractItems(input.copy().setStackSize(size),Actionable.SIMULATE, this.machineSrc);
                    if (item == null)continue;
                    if (item.getStackSize() < size){
                        long size0 = item.getStackSize()/input.getStackSize();
                        if (size0 < 2){
                            this.r$craftingFrequency = 1;
                        } else {
                            this.r$craftingFrequency = size0;
                        }
                    }
                }

                this.r$MediumType = MediumType.MEPatternProvider;
                if (mep.r$isIgnoreParallel()) {
                    this.r$IgnoreParallel = true;
                } else {
                    this.r$IgnoreParallel = false;
                    this.r$craftingFrequency = Math.min(this.remainingOperations, this.r$craftingFrequency);
                }
            } else this.r$MediumType = MediumType.NULL;
        } else if (instance instanceof EFabricatorMEChannel ef) {
            if (!ef.isBusy()){
                var max = 0;
                for (EFabricatorWorker worker : ef.getController().getWorkers()) {
                    max += worker.getRemainingSpace();
                }
                for (IAEItemStack input : r$pattern.getInputs()) {
                    if (input == null)continue;
                    long size = this.r$craftingFrequency;
                    var item = this.inventory.extractItems(input.copy().setStackSize(size),Actionable.SIMULATE, this.machineSrc);
                    if (item == null)continue;
                    if (item.getStackSize() < size){
                        long size0 = item.getStackSize()/input.getStackSize();
                        if (size0 < 2){
                            this.r$craftingFrequency = 1;
                        } else {
                            this.r$craftingFrequency = size0;
                        }
                    }
                }
                this.r$MediumType = MediumType.EF;
                this.r$craftingFrequency = Math.min(max, this.r$craftingFrequency);
            } else this.r$MediumType = MediumType.NULL;
        } else this.r$MediumType = MediumType.NULL;
    }

    @Redirect(method = "executeCrafting",at = @At(value = "INVOKE", target = "Lappeng/api/networking/energy/IEnergyGrid;extractAEPower(DLappeng/api/config/Actionable;Lappeng/api/config/PowerMultiplier;)D"))
    private double extractAEPowerR(IEnergyGrid eg, double v, Actionable actionable, PowerMultiplier powerMultiplier) {
        return switch (this.r$MediumType){
            case MEPatternProvider,EF -> {
                var sum = v * this.r$craftingFrequency;
                var o = eg.extractAEPower(sum,Actionable.SIMULATE,powerMultiplier);
                if (o < sum - 0.01) {
                    long s = (long) (o / sum * this.r$craftingFrequency);
                    this.r$craftingFrequency = s;
                    if (s < 1) {
                        yield eg.extractAEPower(v,actionable,powerMultiplier);
                    } else {
                        yield eg.extractAEPower(v * s,Actionable.SIMULATE,powerMultiplier);
                    }
                }
                yield o;
            }
            default -> eg.extractAEPower(v,actionable,powerMultiplier);
        };
    }

    @Redirect(method = "executeCrafting",at = @At(value = "INVOKE", target = "Lappeng/crafting/MECraftingInventory;extractItems(Lappeng/api/storage/data/IAEItemStack;Lappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)Lappeng/api/storage/data/IAEItemStack;"))
    private IAEItemStack extractItemsR(MECraftingInventory instance, IAEItemStack request, Actionable mode, IActionSource src) {
        return switch (this.r$MediumType) {
            case MEPatternProvider,EF -> {
                var i = request.copy().setStackSize(request.getStackSize() * this.r$craftingFrequency);
                yield instance.extractItems(i, mode, src);
            }
            default -> instance.extractItems(request, mode, src);
        };
    }

    @Unique
    private void r$postChange1(CraftingCPUCluster instance, IAEItemStack receiver, IActionSource single){
        switch (this.r$MediumType) {
            case MEPatternProvider,EF -> {
                var i = receiver.copy().setStackSize(receiver.getStackSize() * this.r$craftingFrequency);
                this.postChange(i, single);
            }
            default -> this.postChange(receiver,single);
        }
    }

    @Unique
    private void r$postChange2(CraftingCPUCluster instance, IAEItemStack receiver, IActionSource single){
        switch (this.r$MediumType) {
            case EF -> {
                var i = receiver.copy().setStackSize(receiver.getStackSize() * this.r$craftingFrequency);
                this.postChange(i, single);
            }
            default -> this.postChange(receiver,single);
        }
    }

    @Redirect(method = "executeCrafting", at = @At(value = "INVOKE", target = "Lappeng/me/cluster/implementations/CraftingCPUCluster;postChange(Lappeng/api/storage/data/IAEItemStack;Lappeng/api/networking/security/IActionSource;)V", ordinal = 1))
    private void postChangeR1(CraftingCPUCluster instance, IAEItemStack receiver, IActionSource single) {
        r$postChange1(instance, receiver, single);
    }

    @Redirect(method = "executeCrafting", at = @At(value = "INVOKE", target = "Lappeng/me/cluster/implementations/CraftingCPUCluster;postChange(Lappeng/api/storage/data/IAEItemStack;Lappeng/api/networking/security/IActionSource;)V", ordinal = 2))
    private void postChangeR2(CraftingCPUCluster instance, IAEItemStack receiver, IActionSource single) {
        r$postChange1(instance, receiver, single);
    }

    @Redirect(method = "executeCrafting", at = @At(value = "INVOKE", target = "Lappeng/me/cluster/implementations/CraftingCPUCluster;postChange(Lappeng/api/storage/data/IAEItemStack;Lappeng/api/networking/security/IActionSource;)V", ordinal = 0))
    private void postChangeR0(CraftingCPUCluster instance, IAEItemStack receiver, IActionSource single) {
        r$postChange2(instance, receiver, single);
    }

    @Redirect(method = "executeCrafting", at = @At(value = "INVOKE", target = "Lappeng/me/cluster/implementations/CraftingCPUCluster;postChange(Lappeng/api/storage/data/IAEItemStack;Lappeng/api/networking/security/IActionSource;)V", ordinal = 3))
    private void postChangeR3(CraftingCPUCluster instance, IAEItemStack receiver, IActionSource single) {
        r$postChange2(instance, receiver, single);
    }

    @Redirect(method = "executeCrafting",at = @At(value = "INVOKE", target = "Lappeng/api/storage/data/IItemList;add(Lappeng/api/storage/data/IAEStack;)V",ordinal = 0))
    private void addR(IItemList<IAEItemStack> instance, IAEStack<IAEItemStack> iaeStack) {
        switch (this.r$MediumType) {
            case MEPatternProvider,EF -> {
                iaeStack.setStackSize(iaeStack.getStackSize() * this.r$craftingFrequency);
                instance.add((IAEItemStack) iaeStack);
            }
            default -> instance.add((IAEItemStack) iaeStack);
        }
    }

    @Redirect(method = "executeCrafting",at = @At(value = "INVOKE", target = "Lappeng/api/storage/data/IItemList;add(Lappeng/api/storage/data/IAEStack;)V",ordinal = 1))
    private void addR1(IItemList<IAEItemStack> instance, IAEStack<IAEItemStack> iaeStack) {
        switch (this.r$MediumType) {
            case EF -> {
                iaeStack.setStackSize(iaeStack.getStackSize() * this.r$craftingFrequency);
                instance.add((IAEItemStack) iaeStack);
            }
            default -> instance.add((IAEItemStack) iaeStack);
        }
    }

    @Redirect(method = "executeCrafting",at = @At(value = "INVOKE", target = "Lappeng/me/cluster/implementations/CraftingCPUCluster;postCraftingStatusChange(Lappeng/api/storage/data/IAEItemStack;)V",ordinal = 0))
    private void postCraftingStatusChangeR(CraftingCPUCluster instance, IAEItemStack iaeStack) {
        switch (this.r$MediumType) {
            case MEPatternProvider,EF -> {
                iaeStack.setStackSize(iaeStack.getStackSize() * this.r$craftingFrequency);
                this.postCraftingStatusChange(iaeStack);
            }
            default -> this.postCraftingStatusChange(iaeStack);
        }
    }

    @Redirect(method = "executeCrafting",at = @At(value = "INVOKE", target = "Lappeng/me/cluster/implementations/CraftingCPUCluster;postCraftingStatusChange(Lappeng/api/storage/data/IAEItemStack;)V",ordinal = 1))
    private void postCraftingStatusChangeR1(CraftingCPUCluster instance, IAEItemStack iaeStack) {
        switch (this.r$MediumType) {
            case EF -> {
                iaeStack.setStackSize(iaeStack.getStackSize() * this.r$craftingFrequency);
                this.postCraftingStatusChange(iaeStack);
            }
            default -> this.postCraftingStatusChange(iaeStack);
        }
    }

    @Redirect(method = "executeCrafting",at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I",remap = true))
    private int getCountR(ItemStack instance) {
        final int out = instance.getCount();
        return switch (this.r$MediumType) {
            case MEPatternProvider,EF -> out / (int) this.r$craftingFrequency;
            default -> out;
        };
    }

    @Redirect(method = "executeCrafting",at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getValue()Ljava/lang/Object;",ordinal = 2))
    private Object getValueR(Map.Entry<ICraftingPatternDetails,AccessorTaskProgress> instance) {
        return switch (this.r$MediumType) {
            case MEPatternProvider,EF -> {
                if (!this.r$IgnoreParallel) {
                    this.remainingOperations -= (int) (this.r$craftingFrequency - 1);
                }
                var value = instance.getValue();
                value.setValue(value.getValue() - (this.r$craftingFrequency - 1));
                yield value;
            }
            default -> instance.getValue();
        };
    }

    @Mixin(targets = "appeng.me.cluster.implementations.CraftingCPUCluster$TaskProgress",remap = false)
    public interface AccessorTaskProgress {
        @Accessor
        long getValue();
        @Accessor
        void setValue(long value);
    }
}
