package github.kasuminova.ecoaeextension.client.gui.widget.efabricator;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.base.WidgetController;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.ecoaeextension.client.gui.GuiEFabricatorPatternSearch;
import github.kasuminova.ecoaeextension.client.gui.widget.InputBox;
import github.kasuminova.ecoaeextension.client.gui.widget.SizedRow;
import github.kasuminova.ecoaeextension.client.gui.widget.efabricator.event.EFPatternSearchContentUpdateEvent;
import net.minecraft.client.resources.I18n;

public class SearchPanel extends SizedRow {

    public static final int WIDTH = 229;
    public static final int HEIGHT = 21;

    public static final TextureProperties BACKGROUND = TextureProperties.of(
            GuiEFabricatorPatternSearch.TEXTURES_ELEMENTS,
            1, 1, WIDTH, HEIGHT
    );

    public SearchPanel(final WidgetController controller) {
        setWidthHeight(WIDTH, HEIGHT);
        InputBox inputContent = new InputBox();
        InputBox outputContent = new InputBox();
        addWidgets(
                inputContent
                        .setEnableBackground(false)
                        .setPrompt(I18n.format("gui.efabricator.pattern_search.input"))
                        .setOnContentChange((inputBox, content) -> controller.postGuiEvent(
                                new EFPatternSearchContentUpdateEvent(inputContent.getText(), outputContent.getText())))
                        .setWidthHeight(88, 10)
                        .setMargin(23, 0, 6, 0),
                outputContent
                        .setEnableBackground(false)
                        .setPrompt(I18n.format("gui.efabricator.pattern_search.output"))
                        .setOnContentChange((inputBox, content) -> controller.postGuiEvent(
                                new EFPatternSearchContentUpdateEvent(inputContent.getText(), outputContent.getText())))
                        .setWidthHeight(88, 10)
                        .setMargin(24, 0, 6, 0)
        );
    }

    @Override
    protected void renderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        BACKGROUND.render(renderPos, gui);
        super.renderInternal(gui, renderSize, renderPos, mousePos);
    }

}
