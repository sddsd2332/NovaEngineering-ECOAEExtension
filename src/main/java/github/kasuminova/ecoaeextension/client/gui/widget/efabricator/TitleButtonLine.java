package github.kasuminova.ecoaeextension.client.gui.widget.efabricator;

import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.Button;
import github.kasuminova.mmce.client.gui.widget.Button4State;
import github.kasuminova.mmce.client.gui.widget.container.Row;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.client.gui.GuiEFabricatorController;
import github.kasuminova.ecoaeextension.common.network.PktEFabricatorGUIAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.util.Collections;

public class TitleButtonLine extends Row {

    public TitleButtonLine(final boolean isSearch) {
        if (isSearch) {
            addWidget(new Button()
                    .setTexture(TextureProperties.of(151, 97))
                    .setHoveredTexture(TextureProperties.of(151, 97))
                    .setTextureLocation(GuiEFabricatorController.TEXTURES_ELEMENTS)
                    .setTooltipFunction(button -> Collections.singletonList(I18n.format("gui.efabricator.switch.default")))
                    .setOnClickedListener(button -> ECOAEExtension.NET_CHANNEL.sendToServer(new PktEFabricatorGUIAction(PktEFabricatorGUIAction.Action.SWITCH_GUI)))
                    .setWidthHeight(14, 9)
            );
        } else {
            addWidget(new Button()
                    .setTexture(TextureProperties.of(151, 87))
                    .setHoveredTexture(TextureProperties.of(151, 87))
                    .setTextureLocation(GuiEFabricatorController.TEXTURES_ELEMENTS)
                    .setTooltipFunction(button -> Collections.singletonList(I18n.format("gui.efabricator.switch.pattern_search")))
                    .setOnClickedListener(button -> ECOAEExtension.NET_CHANNEL.sendToServer(new PktEFabricatorGUIAction(PktEFabricatorGUIAction.Action.SWITCH_GUI)))
                    .setWidthHeight(14, 9)
            );
        }
        addWidget(new Button4State()
                .setMouseDownTexture(TextureProperties.of(183, 75))
                .setTexture(TextureProperties.of(151, 75))
                .setHoveredTexture(TextureProperties.of(167, 75))
                .setTextureLocation(GuiEFabricatorController.TEXTURES_ELEMENTS)
                .setOnClickedListener(button -> Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().player.closeScreen()))
                .setWidthHeight(15, 11));
    }

}
