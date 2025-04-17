package github.kasuminova.ecoaeextension.common.integration.ae2.core;

import net.minecraft.util.text.translation.I18n;

public interface localizationAPI {


    String root();

    default String getLocal() {
        return I18n.translateToLocal(this.getUnlocalized());
    }

    default String getUnlocalized() {
        return this.root() + '.' + this;
    }
}
