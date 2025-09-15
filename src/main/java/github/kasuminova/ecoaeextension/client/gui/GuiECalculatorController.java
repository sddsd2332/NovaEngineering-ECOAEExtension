package github.kasuminova.ecoaeextension.client.gui;

import github.kasuminova.mmce.client.gui.GuiContainerDynamic;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.base.WidgetController;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.client.gui.widget.ecalculator.CPUStatusPanel;
import github.kasuminova.ecoaeextension.client.gui.widget.ecalculator.MonitorPanel;
import github.kasuminova.ecoaeextension.client.gui.widget.ecalculator.StorageBar;
import github.kasuminova.ecoaeextension.client.gui.widget.ecalculator.event.ECGUIDataUpdateEvent;
import github.kasuminova.ecoaeextension.common.container.ContainerECalculatorController;
import github.kasuminova.ecoaeextension.common.container.data.ECalculatorData;
import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorController;
import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiECalculatorController extends GuiContainerDynamic<ContainerECalculatorController> {

    public static final ResourceLocation ELEMENT_1 = new ResourceLocation(ECOAEExtension.MOD_ID, "textures/gui/ecalculator_gui_1.png");
    public static final ResourceLocation ELEMENT_2 = new ResourceLocation(ECOAEExtension.MOD_ID, "textures/gui/ecalculator_gui_2.png");
    private static final TextureProperties BACKGROUND = new TextureProperties(
            new ResourceLocation(ECOAEExtension.MOD_ID, "textures/gui/ecalculator_background.png"),
            0, 0, 255, 221
    );

    @Getter
    private ECalculatorData data = null;

    public GuiECalculatorController(final ECalculatorController controller, final EntityPlayer opening) {
        super(new ContainerECalculatorController(controller, opening));
        this.xSize = 255;
        this.ySize = 221;
        this.widgetController = new WidgetController(WidgetGui.of(this));
        this.widgetController.addWidget(new StorageBar().setAbsXY(7, 7));
        this.widgetController.addWidget(new CPUStatusPanel().setAbsXY(7, 58));
        this.widgetController.addWidget(new MonitorPanel().setAbsXY(7, 137));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        final int x = (this.width - this.xSize) / 2;
        final int y = (this.height - this.ySize) / 2;
        BACKGROUND.render(new RenderPos(x, y), this);
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
    }

    public void onDataUpdate(final ECalculatorData data) {
        this.data = data;
        this.widgetController.postGuiEvent(new ECGUIDataUpdateEvent(this));
    }


}
