package github.kasuminova.ecoaeextension.mixin.mmce;


import com.google.common.collect.Sets;
import github.kasuminova.ecoaeextension.common.util.ITaskExecutor;
import github.kasuminova.mmce.common.concurrent.TaskExecutor;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(TaskExecutor.class)
public abstract class MixinTaskExecutor implements ITaskExecutor {

    @Shadow(remap = false) public abstract int executeActions();

    @Unique
    private final Set<TileEntity> novaeng$requireMarkDirtyTEQueue = Sets.newIdentityHashSet();

//    @Inject(
//            method = "onServerTick",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lgithub/kasuminova/mmce/common/concurrent/TaskExecutor;executeActions()I"
//            ),
//            remap = false
//    )
//    private void injectOnServerTickExecuteActions(final TickEvent.ServerTickEvent event, final CallbackInfo ci) {
//        if (event.phase != TickEvent.Phase.END) {
//            return;
//        }
//        NovaEngineeringCore.PARALLEL_NETWORK_MANAGER.execute();
//        TaskExecutor.totalExecuted += this.executeActions();
//    }

    @Inject(method = "updateTileEntity", at = @At("HEAD"), remap = false)
    private void injectUpdateTE(final CallbackInfo ci) {
        for (final TileEntity te : novaeng$requireMarkDirtyTEQueue) {
            te.markDirty();
        }
        novaeng$requireMarkDirtyTEQueue.clear();
    }

    @Unique
    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public void addTEMarkTask(final TileEntity te) {
        novaeng$requireMarkDirtyTEQueue.add(te);
    }
}
