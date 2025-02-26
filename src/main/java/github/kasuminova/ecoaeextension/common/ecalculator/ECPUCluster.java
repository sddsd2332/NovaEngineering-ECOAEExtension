package github.kasuminova.ecoaeextension.common.ecalculator;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import github.kasuminova.mmce.common.util.TimeRecorder;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.Levels;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorThreadCore;

import javax.annotation.Nullable;

public interface ECPUCluster {

    static ECPUCluster from(final CraftingCPUCluster cluster) {
        return (ECPUCluster) (Object) cluster;
    }

    void novaeng_ec$setAvailableStorage(final long availableStorage);

    void novaeng_ec$setAccelerators(final int accelerators);

    ECalculatorThreadCore novaeng_ec$getController();

    void novaeng_ec$setThreadCore(final ECalculatorThreadCore threadCore);

    void novaeng_ec$setVirtualCPUOwner(@Nullable final ECalculatorController isVirtualCPUOwner);

    Levels novaeng_ec$getControllerLevel();

    long novaeng_ec$getUsedExtraStorage();

    void novaeng_ec$setUsedExtraStorage(final long usedExtraStorage);

    void novaeng_ec$markDestroyed();

    TimeRecorder novaeng_ec$getTimeRecorder();

    TimeRecorder novaeng_ec$getParallelismRecorder();

}
