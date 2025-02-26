package github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum DriveStorageLevel implements IStringSerializable {

    EMPTY("empty"),
    A("a"),
    B("b"),
    C("c");

    public static final PropertyEnum<DriveStorageLevel> STORAGE_LEVEL = PropertyEnum.create("storage_level", DriveStorageLevel.class);
    private final String name;

    DriveStorageLevel(String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

}
