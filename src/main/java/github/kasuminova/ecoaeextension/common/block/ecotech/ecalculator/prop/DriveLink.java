package github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum DriveLink implements IStringSerializable {

    NONE("none"),
    UP("up"),
    DOWN("down");

    public static final PropertyEnum<DriveLink> LINK = PropertyEnum.create("link", DriveLink.class);
    private final String name;

    DriveLink(String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

}
