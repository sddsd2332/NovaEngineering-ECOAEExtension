package github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum ThreadCoreStatus implements IStringSerializable {

    OFF("off"),
    ON("on"),
    RUN("run");

    public static final PropertyEnum<ThreadCoreStatus> STATUS = PropertyEnum.create("status", ThreadCoreStatus.class);
    private final String name;

    ThreadCoreStatus(String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

}

