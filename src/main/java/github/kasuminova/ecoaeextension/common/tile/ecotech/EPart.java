package github.kasuminova.ecoaeextension.common.tile.ecotech;

import javax.annotation.Nullable;

public interface EPart<C extends EPartController<?>> {

    void setController(final EPartController<?> storageController);

    @Nullable
    C getController();

    default boolean isAssembled() {
        return getController() != null;
    }

    void onAssembled();

    void onDisassembled();

}
