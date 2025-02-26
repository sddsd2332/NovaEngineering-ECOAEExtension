package github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum EnergyCellStatus implements IStringSerializable {
    EMPTY("empty"),
    LOW("low"),
    MID("mid"),
    HIGH("high"),
    FULL("full");

    public static final PropertyEnum<EnergyCellStatus> STATUS = PropertyEnum.create("status", EnergyCellStatus.class);
    private final String name;

    EnergyCellStatus(String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }
}
