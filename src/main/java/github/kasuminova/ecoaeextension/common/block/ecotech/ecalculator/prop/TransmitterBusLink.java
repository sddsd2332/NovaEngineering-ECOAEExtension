package github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum TransmitterBusLink implements IStringSerializable {

    NONE("none"),
    UP("up"),
    DOWN("down"),
    ALL("all");

    public static final PropertyEnum<TransmitterBusLink> LINK = PropertyEnum.create("link", TransmitterBusLink.class);
    private final String name;

    TransmitterBusLink(String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

}
