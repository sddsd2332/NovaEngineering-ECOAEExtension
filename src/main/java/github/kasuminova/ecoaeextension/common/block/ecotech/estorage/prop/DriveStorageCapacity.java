package github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum DriveStorageCapacity implements IStringSerializable {
    EMPTY("empty"),
    TYPE_MAX("type_max"),
    FULL("full");

    public static final PropertyEnum<DriveStorageCapacity> STORAGE_CAPACITY = PropertyEnum.create("storage_capacity", DriveStorageCapacity.class);
    private final String name;

    DriveStorageCapacity(String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }
}
