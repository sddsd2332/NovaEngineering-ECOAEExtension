package github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum DriveStorageType implements IStringSerializable {
    EMPTY("empty"),
    ITEM("item"),
    FLUID("fluid");

    public static final PropertyEnum<DriveStorageType> STORAGE_TYPE = PropertyEnum.create("storage_type", DriveStorageType.class);
    private final String name;

    DriveStorageType(String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }
}
