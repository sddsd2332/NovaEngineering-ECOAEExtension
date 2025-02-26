package github.kasuminova.ecoaeextension.common.block.ecotech.efabricator.prop;

public enum Levels {

    L4(4,  4),
    L6(8,  8),
    L9(16, 16);

    private final int queueDepthMul;
    private final int energyUsageMul;

    Levels(final int queueDepthMul, final int energyUsageMul) {
        this.queueDepthMul = queueDepthMul;
        this.energyUsageMul = energyUsageMul;
    }

    public int applyOverclockQueueDepth(final int value) {
        return value * this.queueDepthMul;
    }

    public int applyOverclockEnergyUsage(final int value) {
        return value * this.energyUsageMul;
    }

}
