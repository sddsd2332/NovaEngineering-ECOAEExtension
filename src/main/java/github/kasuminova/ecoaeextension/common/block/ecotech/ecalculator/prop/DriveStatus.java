package github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum DriveStatus implements IStringSerializable {

    OFF("off"),
    ON("on");

    public static final PropertyEnum<DriveStatus> STATUS = PropertyEnum.create("status", DriveStatus.class);
    private final String name;

    DriveStatus(String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

}
