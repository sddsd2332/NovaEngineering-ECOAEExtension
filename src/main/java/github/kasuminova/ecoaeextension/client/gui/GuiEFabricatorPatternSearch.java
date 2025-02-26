package github.kasuminova.ecoaeextension.client.gui;

import github.kasuminova.mmce.client.gui.GuiContainerDynamic;
import github.kasuminova.mmce.client.gui.widget.MultiLineLabel;
import github.kasuminova.mmce.client.gui.widget.base.WidgetController;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.client.gui.widget.efabricator.PatternPanel;
import github.kasuminova.ecoaeextension.client.gui.widget.efabricator.SearchPanel;
import github.kasuminova.ecoaeextension.client.gui.widget.efabricator.TitleButtonLine;
import github.kasuminova.ecoaeextension.client.gui.widget.efabricator.event.EFPatternSearchGUIUpdateEvent;
import github.kasuminova.ecoaeextension.common.container.ContainerEFabricatorPatternSearch;
import github.kasuminova.ecoaeextension.common.container.data.EFabricatorPatternData;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorController;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;

public class GuiEFabricatorPatternSearch extends GuiContainerDynamic<ContainerEFabricatorPatternSearch> {

    public static final ResourceLocation TEXTURES_BACKGROUND_DARK = new ResourceLocation(
            ECOAEExtension.MOD_ID, "textures/gui/efabricator_pattern_search_bg_dark.png");
    public static final ResourceLocation TEXTURES_ELEMENTS = new ResourceLocation(
            ECOAEExtension.MOD_ID, "textures/gui/efabricator_search.png");
    public static final ResourceLocation TEXTURES_INVENTORY = new ResourceLocation(
            ECOAEExtension.MOD_ID, "textures/gui/efabricator_inventory.png");

    private EFabricatorPatternData data = null;

    public GuiEFabricatorPatternSearch(final EFabricatorController controller, final EntityPlayer opening) {
        super(new ContainerEFabricatorPatternSearch(controller, opening));
        this.xSize = 243;
        this.ySize = 202 + 10;
        this.widgetController = new WidgetController(WidgetGui.of(this));
        this.widgetController.addWidget(new MultiLineLabel(Collections.singletonList(I18n.format("gui.efabricator.pattern_search.title")))
                .setScale(.8f).setAutoWrap(false).setAbsXY(7, 3)
        );
        this.widgetController.addWidget(new SearchPanel(this.widgetController).setAbsXY(7, 15));
        this.widgetController.addWidget(new PatternPanel().setAbsXY(7, 39));
        final TitleButtonLine buttonLine = new TitleButtonLine(true);
        this.widgetController.addWidget(buttonLine.setAbsXY(this.xSize - buttonLine.getWidth() - 1, 1));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES_BACKGROUND_DARK);
        final int x = (this.width - this.xSize) / 2;
        final int y = (this.height - this.ySize) / 2;
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 256, 202 + 10, 256, 256);
        this.mc.getTextureManager().bindTexture(TEXTURES_INVENTORY);
        Gui.drawModalRectWithCustomSizedTexture(x + 17, y + 118 + 11, 1, 120, 162, 76, 256, 256);
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
    }

    public void onDataUpdate(final EFabricatorPatternData data, final boolean fullUpdate) {
        this.data = data;
        this.widgetController.postGuiEvent(new EFPatternSearchGUIUpdateEvent(this, fullUpdate));
    }

    public EFabricatorPatternData getData() {
        return data;
    }

}
