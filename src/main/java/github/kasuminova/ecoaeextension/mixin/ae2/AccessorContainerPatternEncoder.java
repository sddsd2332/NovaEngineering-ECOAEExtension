package github.kasuminova.ecoaeextension.mixin.ae2;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.container.implementations.ContainerPatternEncoder;
import appeng.container.slot.SlotRestrictedInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ContainerPatternEncoder.class, remap = false)
public interface AccessorContainerPatternEncoder {

    @Accessor
    SlotRestrictedInput getPatternSlotOUT();

    @Accessor
    IGuiItemObject getIGuiItemObject();

}
