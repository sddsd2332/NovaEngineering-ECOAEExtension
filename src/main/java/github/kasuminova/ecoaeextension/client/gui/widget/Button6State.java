package github.kasuminova.ecoaeextension.client.gui.widget;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.Button5State;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;

import java.util.Optional;

public class Button6State extends Button5State {

    protected TextureProperties clickedMouseDownTexture = TextureProperties.EMPTY;

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        if (!isVisible()) {
            return;
        }
        if (isUnavailable()) {
            unavailableTexture.render(textureLocation, renderPos, renderSize, gui);
            return;
        }
        if (clicked) {
            if (mouseDown) {
                clickedMouseDownTexture.render(textureLocation, renderPos, renderSize, gui);
            } else {
                clickedTexture.render(textureLocation, renderPos, renderSize, gui);
            }
            return;
        }
        if (mouseDown) {
            mouseDownTexture.render(textureLocation, renderPos, renderSize, gui);
            return;
        }
        if (isMouseOver(mousePos)) {
            hoveredTexture.render(textureLocation, renderPos, renderSize, gui);
            return;
        }
        texture.render(textureLocation, renderPos, renderSize, gui);
    }

    public TextureProperties getClickedMouseDownTexture() {
        return clickedMouseDownTexture;
    }

    public Button6State setClickedMouseDownTexture(final int clickedTextureX, final int clickedTextureY) {
        return setClickedMouseDownTexture(TextureProperties.of(clickedTextureX, clickedTextureY));
    }

    public Button6State setClickedMouseDownTexture(final TextureProperties clickedMouseDownTexture) {
        this.clickedMouseDownTexture = Optional.ofNullable(clickedMouseDownTexture).orElse(TextureProperties.EMPTY);
        return this;
    }

}
