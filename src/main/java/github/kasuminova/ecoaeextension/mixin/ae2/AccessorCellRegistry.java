package github.kasuminova.ecoaeextension.mixin.ae2;

import appeng.api.storage.ICellHandler;
import appeng.core.features.registries.cell.CellRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CellRegistry.class)
public interface AccessorCellRegistry {

    @Accessor(remap = false)
    List<ICellHandler> getHandlers();

}
