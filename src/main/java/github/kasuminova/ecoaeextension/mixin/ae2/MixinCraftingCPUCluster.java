package github.kasuminova.ecoaeextension.mixin.ae2;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionSource;
import appeng.crafting.MECraftingInventory;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import appeng.tile.crafting.TileCraftingTile;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import github.kasuminova.mmce.common.util.TimeRecorder;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.Levels;
import github.kasuminova.ecoaeextension.common.ecalculator.ECPUCluster;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorMEChannel;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorThreadCore;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public abstract class MixinCraftingCPUCluster implements ECPUCluster {

    @Unique
    private ECalculatorThreadCore novaeng_ec$core = null;

    @Unique
    private ECalculatorController novaeng_ec$virtualCPUOwner = null;

    @Unique
    private long novaeng_ec$usedExtraStorage = 0;

    @Unique
    private final TimeRecorder novaeng_ec$timeRecorder = new TimeRecorder();

    @Unique
    private final TimeRecorder novaeng_ec$parallelismRecorder = new TimeRecorder();

    @Shadow
    private long availableStorage;

    @Shadow
    private boolean isDestroyed;

    @Shadow
    private int accelerator;

    @Shadow
    private MECraftingInventory inventory;

    @Shadow
    private boolean isComplete;

    @Shadow
    private ICraftingLink myLastLink;

    @Shadow
    private MachineSource machineSrc;

    @Shadow
    public abstract void destroy();

    @Shadow
    public abstract void cancel();

    @Final
    @Shadow
    private int[] usedOps;

    @Inject(method = "submitJob", at = @At(value = "INVOKE", target = "Lappeng/api/networking/crafting/ICraftingJob;getOutput()Lappeng/api/storage/data/IAEItemStack;"))
    private void injectSubmitJob(final IGrid g, final ICraftingJob job, final IActionSource src, final ICraftingRequester requestingMachine, final CallbackInfoReturnable<ICraftingLink> cir) {
        if (this.novaeng_ec$virtualCPUOwner == null) {
            return;
        }
        this.novaeng_ec$virtualCPUOwner.onVirtualCPUSubmitJob(job.getByteTotal());
    }

    @Inject(method = "cancel", at = @At("RETURN"))
    private void injectCancel(final CallbackInfo ci) {
        if (this.novaeng_ec$core == null) {
            return;
        }
        // Ensure inventory is empty
        if (this.inventory.getItemList().isEmpty()) {
            destroy();
        }
    }

    @Inject(method = "updateCraftingLogic", at = @At("HEAD"), cancellable = true)
    private void injectUpdateCraftingLogicStoreItems(final IGrid grid, final IEnergyGrid eg, final CraftingGridCache cgc, final CallbackInfo ci) {
        if (this.novaeng_ec$core == null) {
            return;
        }
        if (this.myLastLink != null) {
            if (this.myLastLink.isCanceled()) {
                this.myLastLink = null;
                this.cancel();
            }
        }
        if (this.isComplete) {
            // Ensure inventory is empty
            if (this.inventory.getItemList().isEmpty()) {
                destroy();
                ci.cancel();
            }
        }
    }

    @Inject(method = "updateCraftingLogic", at = @At("TAIL"))
    private void injectUpdateCraftingLogicTail(final IGrid grid, final IEnergyGrid eg, final CraftingGridCache cgc, final CallbackInfo ci) {
        int currentParallelism = this.usedOps[0];
        novaeng_ec$parallelismRecorder.addUsedTime(currentParallelism);
    }

    @WrapOperation(
            method = "updateCraftingLogic",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/tile/crafting/TileCraftingTile;isActive()Z"
            )
    )
    private boolean redirectUpdateCraftingLogicIsActive(final TileCraftingTile instance, final Operation<Boolean> original) {
        if (this.novaeng_ec$core != null) {
            ECalculatorController controller = this.novaeng_ec$core.getController();
            return controller != null && controller.getChannel() != null && controller.getChannel().getProxy().isActive();
        }
        if (this.novaeng_ec$virtualCPUOwner != null) {
            ECalculatorController controller = novaeng_ec$virtualCPUOwner;
            return controller.getChannel() != null && controller.getChannel().getProxy().isActive();
        }
        return original.call(instance);
    }

    @Inject(method = "destroy", at = @At("HEAD"), cancellable = true)
    private void injectDestroy(final CallbackInfo ci) {
        if (this.novaeng_ec$core == null) {
            return;
        }
        if (this.isDestroyed) {
            ci.cancel();
            return;
        }
        this.novaeng_ec$core.onCPUDestroyed((CraftingCPUCluster) (Object) this);
    }

    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private void injectIsActive(final CallbackInfoReturnable<Boolean> cir) {
        if (this.novaeng_ec$core == null && this.novaeng_ec$virtualCPUOwner == null) {
            return;
        }
        if (this.novaeng_ec$core != null) {
            ECalculatorController controller = this.novaeng_ec$core.getController();
            cir.setReturnValue(controller != null && controller.getChannel() != null && controller.getChannel().getProxy().isActive());
        }
        if (this.novaeng_ec$virtualCPUOwner != null) {
            ECalculatorController controller = novaeng_ec$virtualCPUOwner;
            cir.setReturnValue(controller.getChannel() != null && controller.getChannel().getProxy().isActive());
        }
    }

    @Inject(method = "getGrid", at = @At("HEAD"), cancellable = true)
    private void injectGetGrid(final CallbackInfoReturnable<IGrid> cir) {
        if (this.novaeng_ec$core != null) {
            final ECalculatorController controller = this.novaeng_ec$core.getController();
            if (controller == null) {
                return;
            }
            final ECalculatorMEChannel channel = controller.getChannel();
            if (channel == null) {
                return;
            }
            IGridNode node = channel.getProxy().getNode();
            cir.setReturnValue(node == null ? null : node.getGrid());
        }
        if (this.novaeng_ec$virtualCPUOwner != null) {
            final ECalculatorMEChannel channel = novaeng_ec$virtualCPUOwner.getChannel();
            if (channel == null) {
                return;
            }
            IGridNode node = channel.getProxy().getNode();
            cir.setReturnValue(node == null ? null : node.getGrid());
        }
    }

    @Inject(method = "getCore", at = @At("HEAD"), cancellable = true)
    private void injectGetCore(final CallbackInfoReturnable<TileCraftingTile> cir) {
        if (this.novaeng_ec$core != null || this.novaeng_ec$virtualCPUOwner != null) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "getWorld", at = @At("HEAD"), cancellable = true)
    private void injectGetWorld(final CallbackInfoReturnable<World> cir) {
        if (this.novaeng_ec$core != null) {
            cir.setReturnValue(this.novaeng_ec$core.getWorld());
        }
        if (this.novaeng_ec$virtualCPUOwner != null) {
            cir.setReturnValue(novaeng_ec$virtualCPUOwner.getWorld());
        }
    }

    @Inject(method = "markDirty", at = @At("HEAD"), cancellable = true)
    private void injectMarkDirty(final CallbackInfo ci) {
        if (this.novaeng_ec$core != null) {
            this.novaeng_ec$core.markNoUpdateSync();
            ci.cancel();
        }
        if (this.novaeng_ec$virtualCPUOwner != null) {
            this.novaeng_ec$virtualCPUOwner.markNoUpdateSync();
            ci.cancel();
        }
    }

    @Unique
    @Override
    public void novaeng_ec$setAvailableStorage(final long availableStorage) {
        this.availableStorage = availableStorage;
    }

    @Unique
    @Override
    public void novaeng_ec$setAccelerators(final int accelerators) {
        this.accelerator = accelerators;
    }

    @Unique
    @Override
    public ECalculatorThreadCore novaeng_ec$getController() {
        return novaeng_ec$core;
    }

    @Unique
    @Override
    public void novaeng_ec$setThreadCore(final ECalculatorThreadCore threadCore) {
        this.novaeng_ec$core = threadCore;

        final ECalculatorController controller = threadCore.getController();
        if (controller == null) {
            return;
        }
        final ECalculatorMEChannel channel = controller.getChannel();
        if (channel != null) {
            this.machineSrc = new MachineSource(channel);
        }
    }

    @Unique
    @Override
    public void novaeng_ec$setVirtualCPUOwner(@Nullable final ECalculatorController virtualCPUOwner) {
        this.novaeng_ec$virtualCPUOwner = virtualCPUOwner;
        if (virtualCPUOwner == null) {
            return;
        }

        final ECalculatorMEChannel channel = virtualCPUOwner.getChannel();
        if (channel != null) {
            this.machineSrc = new MachineSource(channel);
        }
    }

    @Unique
    @Override
    public Levels novaeng_ec$getControllerLevel() {
        final ECalculatorController controller;
        if (this.novaeng_ec$core != null) {
            controller = this.novaeng_ec$core.getController();
        } else if (this.novaeng_ec$virtualCPUOwner != null) {
            controller = this.novaeng_ec$virtualCPUOwner;
        } else {
            return null;
        }

        if (controller != null) {
            return controller.getLevel();
        }
        return null;
    }

    @Unique
    @Override
    public void novaeng_ec$setUsedExtraStorage(final long usedExtraStorage) {
        this.novaeng_ec$usedExtraStorage = usedExtraStorage;
    }

    @Unique
    @Override
    public long novaeng_ec$getUsedExtraStorage() {
        return novaeng_ec$usedExtraStorage;
    }

    @Unique
    @Override
    public void novaeng_ec$markDestroyed() {
        this.isDestroyed = true;
        this.isComplete = true;
    }

    @Unique
    @Override
    public TimeRecorder novaeng_ec$getTimeRecorder() {
        return novaeng_ec$timeRecorder;
    }

    @Unique
    @Override
    public TimeRecorder novaeng_ec$getParallelismRecorder() {
        return novaeng_ec$parallelismRecorder;
    }

}
