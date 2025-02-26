package github.kasuminova.ecoaeextension.client.gui.widget.efabricator;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.container.Row;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.ecoaeextension.client.gui.GuiEFabricatorController;
import github.kasuminova.ecoaeextension.client.gui.widget.SizedColumn;
import github.kasuminova.ecoaeextension.client.gui.widget.efabricator.event.EFGUIDataUpdateEvent;
import github.kasuminova.ecoaeextension.common.container.data.EFabricatorData;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorWorker;

import java.util.List;

public class CraftingStatusPanel extends SizedColumn {

    public static final int WIDTH = 229;
    public static final int HEIGHT = 58;

    public static final TextureProperties TEXTURE_BACKGROUND = new TextureProperties(
            GuiEFabricatorController.TEXTURES_INVENTORY,
            1, 197, WIDTH, HEIGHT
    );

    public static final int MAX_COL = 12;
    public static final int MAX_ROW = 3;

    public static final int PROC_ROW_WIDGET_IDX_0 = 0;
    public static final int PROC_ROW_WIDGET_IDX_1 = 2;
    public static final int WORKER_ROW_WIDGET_IDX = 1;

    protected int length = 0;

    public CraftingStatusPanel() {
        setWidthHeight(WIDTH, HEIGHT);
        rebuildWidgets(0);
    }

    private void rebuildWidgets(final int len) {
        widgets.clear();

        Row row = new Row();
        for (int c = 0; c < MAX_COL; c++) {
            row.addWidget(new ParallelProcStatus(len > c).setMargin(1, 0, 1, 0));
        }
        addWidget(row);

        row = new Row();
        for (int c = 0; c < MAX_COL; c++) {
            row.addWidget(new CraftingStatus(len > c).setMargin(1, 0, 1, 0));
        }
        addWidget(row);

        row = new Row();
        for (int c = 0; c < MAX_COL; c++) {
            row.addWidget(new ParallelProcStatus(len > c).setMargin(1, 0, 1, 0));
        }
        addWidget(row);

        this.length = len;
    }

    @Override
    public boolean onGuiEvent(final GuiEvent event) {
        if (event instanceof EFGUIDataUpdateEvent efGUIEvent) {
            rebuildStatus(efGUIEvent.getEFGui().getData());
        }
        return super.onGuiEvent(event);
    }

    private void rebuildStatus(final EFabricatorData data) {
        List<DynamicWidget> widgets = getWidgets();
        List<EFabricatorData.WorkerStatus> workers = data.workers();
        int length = data.length();
        if (this.length != length) {
            rebuildWidgets(length);
        }

        for (int i = 0; i < length; i++) {
            Row row = (Row) widgets.get(WORKER_ROW_WIDGET_IDX);
            DynamicWidget widget = row.getWidgets().get(i);
            if (widget instanceof CraftingStatus status) {
                EFabricatorData.WorkerStatus workerStatus = workers.get(i);
                int queueDepth = EFabricatorWorker.MAX_QUEUE_DEPTH;
                status.update(workerStatus.queueLength(), data.overclocked() ? data.level().applyOverclockQueueDepth(queueDepth) : queueDepth, workerStatus.crafting());
            }

            row = (Row) widgets.get(PROC_ROW_WIDGET_IDX_0);
            widget = row.getWidgets().get(i);
            if (widget instanceof ParallelProcStatus procStatus) {
                procStatus.setLevel(data.level());
            }

            row = (Row) widgets.get(PROC_ROW_WIDGET_IDX_1);
            widget = row.getWidgets().get(i);
            if (widget instanceof ParallelProcStatus procStatus) {
                procStatus.setLevel(data.level());
            }
        }
    }

    @Override
    protected void preRenderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        TEXTURE_BACKGROUND.render(renderPos, gui);
        super.preRenderInternal(gui, renderSize, renderPos, mousePos);
    }

}
