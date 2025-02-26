package github.kasuminova.ecoaeextension.client.gui.widget.efabricator;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.ecoaeextension.client.gui.GuiEFabricatorController;
import github.kasuminova.ecoaeextension.common.block.ecotech.efabricator.prop.Levels;

public class ParallelProcStatus extends DynamicWidget {

    public static final TextureProperties L4 = new TextureProperties(
            GuiEFabricatorController.TEXTURES_ELEMENTS,
            1, 145, 18, 18
    );

    public static final TextureProperties L6 = new TextureProperties(
            GuiEFabricatorController.TEXTURES_ELEMENTS,
            20, 145, 18, 18
    );

    public static final TextureProperties L9 = new TextureProperties(
            GuiEFabricatorController.TEXTURES_ELEMENTS,
            39, 145, 18, 18
    );

    private static final TextureProperties TEXTURE_UNAVAILABLE = new TextureProperties(
            GuiEFabricatorController.TEXTURES_ELEMENTS,
            58, 126, 18, 18
    );

    protected Levels level = Levels.L4;
    protected boolean available;

    public ParallelProcStatus(boolean available) {
        setWidthHeight(18, 18);
        this.available = available;
    }

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        if (!available) {
            TEXTURE_UNAVAILABLE.renderIfPresent(renderPos, gui);
            return;
        }
        switch (level) {
            case L4 -> L4.renderIfPresent(renderPos, gui);
            case L6 -> L6.renderIfPresent(renderPos, gui);
            case L9 -> L9.renderIfPresent(renderPos, gui);
        }
    }

    public ParallelProcStatus setLevel(Levels level) {
        this.level = level;
        return this;
    }

    public boolean isAvailable() {
        return available;
    }

    public ParallelProcStatus setAvailable(final boolean available) {
        this.available = available;
        return this;
    }

}
