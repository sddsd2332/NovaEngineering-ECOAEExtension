package github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum ParallelProcStatus implements IStringSerializable {

    OFF("off"),
    ON("on");

    public static final PropertyEnum<ParallelProcStatus> STATUS = PropertyEnum.create("status", ParallelProcStatus.class);
    private final String name;

    ParallelProcStatus(String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

}
