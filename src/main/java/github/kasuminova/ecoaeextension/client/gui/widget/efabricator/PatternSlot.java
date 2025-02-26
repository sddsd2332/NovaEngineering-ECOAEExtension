package github.kasuminova.ecoaeextension.client.gui.widget.efabricator;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.slot.SlotItemVirtual;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.client.gui.GuiEFabricatorController;
import github.kasuminova.ecoaeextension.common.network.PktEFabricatorPatternSearchGUIAction;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.WeakHashMap;

public class PatternSlot extends SlotItemVirtual {

    private static final WeakHashMap<IAEItemStack, WeakReference<ICraftingPatternDetails>> DETAILS_CACHE = new WeakHashMap<>();

    private final BlockPos slotOwnerPos;
    private final int slotIndex;

    private ICraftingPatternDetails details = null;

    public PatternSlot(final BlockPos slotOwnerPos, final int slotIndex) {
        this.slotOwnerPos = slotOwnerPos;
        this.slotIndex = slotIndex;
        setSlotTex(TextureProperties.of(GuiEFabricatorController.TEXTURES_INVENTORY, 1, 120, 18, 18));
    }

    @Override
    public void render(final WidgetGui widgetGui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        final ItemStack prevStack = stackInSlot;
        if (details != null) {
            IAEItemStack primaryOutput = details.getPrimaryOutput();
            stackInSlot = primaryOutput.getCachedItemStack(primaryOutput.getStackSize());
        }
        super.render(widgetGui, renderSize, renderPos, mousePos);
        stackInSlot = prevStack;
    }

    @Override
    public boolean onMouseClick(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        if (stackInSlot.isEmpty()) {
            return false;
        }
        ECOAEExtension.NET_CHANNEL.sendToServer(new PktEFabricatorPatternSearchGUIAction(
                PktEFabricatorPatternSearchGUIAction.Action.PICKUP_PATTERN, slotOwnerPos, slotIndex
        ));
        return true;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof final PatternSlot other)) {
            return false;
        }
        return slotOwnerPos.equals(other.slotOwnerPos) && slotIndex == other.slotIndex;
    }

    @Override
    public SlotItemVirtual setStackInSlot(final ItemStack stackInSlot) {
        if (stackInSlot.getItem() instanceof ICraftingPatternItem patternItem) {
            AEItemStack key = AEItemStack.fromItemStack(stackInSlot);
            if (key == null) {
                this.details = null;
                return super.setStackInSlot(stackInSlot);
            }

            if (DETAILS_CACHE.containsKey(key)) {
                WeakReference<ICraftingPatternDetails> ref = DETAILS_CACHE.get(key);
                if (ref != null) {
                    this.details = ref.get();
                } else {
                    this.details = patternItem.getPatternForItem(stackInSlot, Minecraft.getMinecraft().world);
                    DETAILS_CACHE.put(key.copy(), new WeakReference<>(this.details));
                }
            } else {
                this.details = patternItem.getPatternForItem(stackInSlot, Minecraft.getMinecraft().world);
                DETAILS_CACHE.put(key.copy(), new WeakReference<>(this.details));
            }
        } else {
            this.details = null;
        }

        return super.setStackInSlot(stackInSlot);
    }

    @Nullable
    public ICraftingPatternDetails getDetails() {
        return details;
    }

    @Override
    public int hashCode() {
        return Objects.hash(slotOwnerPos, slotIndex);
    }

}
