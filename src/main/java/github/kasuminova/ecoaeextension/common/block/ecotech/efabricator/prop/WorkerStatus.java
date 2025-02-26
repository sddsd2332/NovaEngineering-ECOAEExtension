package github.kasuminova.ecoaeextension.common.block.ecotech.efabricator.prop;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum WorkerStatus implements IStringSerializable {
    OFF("off"),
    ON("on"),
    RUN("run");

    public static final PropertyEnum<WorkerStatus> STATUS = PropertyEnum.create("status", WorkerStatus.class);
    private final String name;

    WorkerStatus(String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }
}
