package github.kasuminova.ecoaeextension.client.gui;

import github.kasuminova.mmce.client.gui.GuiContainerDynamic;
import github.kasuminova.mmce.client.gui.widget.base.WidgetController;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.container.WidgetContainer;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.client.gui.widget.estorage.CellInfoColumn;
import github.kasuminova.ecoaeextension.client.gui.widget.estorage.EStorageGraph;
import github.kasuminova.ecoaeextension.client.gui.widget.estorage.Graph;
import github.kasuminova.ecoaeextension.client.gui.widget.estorage.event.ESGUIDataUpdateEvent;
import github.kasuminova.ecoaeextension.client.gui.widget.estorage.event.ESGraphFocusUpdateEvent;
import github.kasuminova.ecoaeextension.common.container.ContainerEStorageController;
import github.kasuminova.ecoaeextension.common.container.data.EStorageCellData;
import github.kasuminova.ecoaeextension.common.container.data.EStorageEnergyData;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageController;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class GuiEStorageController extends GuiContainerDynamic<ContainerEStorageController> {
    public static final ResourceLocation TEXTURES_BACKGROUND = new ResourceLocation(
            ECOAEExtension.MOD_ID, "textures/gui/estorage_controller.png");

    protected List<EStorageCellData> cellDataList = new ArrayList<>();
    protected EStorageEnergyData energyData = null;

    public GuiEStorageController(final EStorageController controller, final EntityPlayer opening) {
        super(new ContainerEStorageController(controller, opening));
        this.xSize = 256;
        this.ySize = 207;
        this.widgetController = new WidgetController(WidgetGui.of(this));
        this.widgetController.addWidgetContainer((WidgetContainer) new CellInfoColumn(this).setAbsXY(178, 22));
        this.widgetController.addWidgetContainer((WidgetContainer) new EStorageGraph(this).setAbsXY(63, 22));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES_BACKGROUND);
        final int x = (this.width - this.xSize) / 2;
        final int y = (this.height - this.ySize) / 2;
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 256, 207, 256, 256);
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
    }


    public void onDataReceived() {
        this.widgetController.postGuiEvent(new ESGUIDataUpdateEvent());
    }

    public void onGraphFocusUpdate(final Graph graph) {
        this.widgetController.postGuiEvent(new ESGraphFocusUpdateEvent(graph));
    }

    public WidgetController getWidgetController() {
        return widgetController;
    }

}