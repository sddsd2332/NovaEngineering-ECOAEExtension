package github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum TransmitterBusLinkLevel implements IStringSerializable {

    NONE("none"),
    L4("l4"),
    L6("l6"),
    L9("l9");

    public static final PropertyEnum<TransmitterBusLinkLevel> LINK_LEVEL = PropertyEnum.create("link_level", TransmitterBusLinkLevel.class);
    private final String name;

    TransmitterBusLinkLevel(String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

}
