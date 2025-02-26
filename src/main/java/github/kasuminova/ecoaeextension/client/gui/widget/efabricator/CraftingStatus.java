package github.kasuminova.ecoaeextension.client.gui.widget.efabricator;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.ecoaeextension.client.gui.GuiEFabricatorController;
import github.kasuminova.ecoaeextension.client.gui.widget.ProgressBar;
import github.kasuminova.ecoaeextension.client.gui.widget.SizedColumn;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.Collections;

public class CraftingStatus extends SizedColumn {

    private static final TextureProperties TEXTURE_BACKGROUND = new TextureProperties(
            GuiEFabricatorController.TEXTURES_ELEMENTS,
            20, 126, 18, 18
    );

    private static final TextureProperties TEXTURE_BACKGROUND_DISABLED = new TextureProperties(
            GuiEFabricatorController.TEXTURES_ELEMENTS,
            39, 126, 18, 18
    );

    private static final TextureProperties TEXTURE_UNAVAILABLE = new TextureProperties(
            GuiEFabricatorController.TEXTURES_ELEMENTS,
            58, 126, 18, 18
    );

    private static final TextureProperties TEXTURE_PROGRESS_FOREGROUND = new TextureProperties(
            GuiEFabricatorController.TEXTURES_ELEMENTS,
            77, 126, 14, 3
    );

    private final SlotItemVirtualJEISmall slot = SlotItemVirtualJEISmall.of();
    private final ProgressBar progress = new ProgressBar();

    private boolean available;

    public CraftingStatus(boolean available) {
        setWidthHeight(18, 18);

        slot.setMargin(6, 0, 2, 1);
        progress.setLeftToRight(true)
                .setHorizontal(true)
                .addForegroundTexture(TEXTURE_PROGRESS_FOREGROUND)
                .setMaxProgress(100)
                .setTooltipFunction(input -> Collections.singletonList(
                        I18n.format("gui.efabricator.queue.status", (int) input.getProgress(), (int) input.getMaxProgress())
                ))
                .setWidthHeight(14, 3)
                .setMarginLeft(2);

        this.available = available;

        if (!available) {
            return;
        }
        addWidgets(slot, progress);
    }

    public void update(final int queueLen, final int maxQueueLen, final ItemStack crafting) {
        slot.setStackInSlot(crafting);
        progress.setMaxProgress(maxQueueLen)
                .setProgress(queueLen);
    }

    @Override
    protected void preRenderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        if (!available) {
            TEXTURE_UNAVAILABLE.render(renderPos, gui.getGui());
        } else if (progress.getProgress() > 0) {
            TEXTURE_BACKGROUND.render(renderPos, gui.getGui());
        } else {
            TEXTURE_BACKGROUND_DISABLED.render(renderPos, gui.getGui());
        }
        super.preRenderInternal(gui, renderSize, renderPos, mousePos);
    }

    public ProgressBar getProgress() {
        return progress;
    }

    public boolean isAvailable() {
        return available;
    }

    public CraftingStatus setAvailable(final boolean available) {
        this.available = available;
        return this;
    }

}
