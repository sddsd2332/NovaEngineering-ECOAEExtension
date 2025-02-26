package github.kasuminova.ecoaeextension.client.gui.widget.estorage;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.container.ScrollingColumn;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.ecoaeextension.client.gui.GuiEStorageController;
import github.kasuminova.ecoaeextension.client.gui.widget.estorage.event.ESGUIDataUpdateEvent;
import github.kasuminova.ecoaeextension.common.container.data.EStorageCellData;

import java.util.Iterator;

public class CellInfoColumn extends ScrollingColumn {

    protected final GuiEStorageController controllerGUI;

    public CellInfoColumn(final GuiEStorageController controllerGUI) {
        this.controllerGUI = controllerGUI;
        setWidthHeight(68, 172);
        scrollbar.setDisabled(true);
        scrollbar.setMouseWheelCheckPos(false);
    }

    @Override
    public void update(final WidgetGui gui) {
        super.update(gui);
        int size = getWidgets().size();
        if (size > 0) {
            scrollbar.setScrollUnit(scrollbar.getRange() / size);
        }
    }

    @Override
    public boolean onGuiEvent(final GuiEvent event) {
        if (event instanceof ESGUIDataUpdateEvent) {
            getWidgets().clear();
            for (Iterator<EStorageCellData> it = controllerGUI.getCellDataList().iterator(); it.hasNext(); ) {
                final EStorageCellData data = it.next();
                EStorageCellInfo info = new EStorageCellInfo(data);
                if (!it.hasNext()) {
                    info.setMarginDown(0);
                }
                addWidget(info);
            }
        }
        return super.onGuiEvent(event);
    }

    @Override
    public boolean onMouseDWheel(final MousePos mousePos, final RenderPos renderPos, final int wheel) {
        int width = this.width;
        int height = this.height;

        int y = getTotalHeight() > height ? -scrollbar.getCurrentScroll() : 0;

        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            RenderPos widgetRenderPos = getWidgetRenderOffset(widget, width, y);
            if (widgetRenderPos == null) {
                continue;
            }
            RenderPos absRenderPos = widgetRenderPos.add(renderPos);
            if (widget.onMouseDWheel(mousePos.relativeTo(widgetRenderPos), absRenderPos, wheel)) {
                return true;
            }
            y += widget.getMarginUp() + widget.getHeight() + widget.getMarginDown();
        }

        if (isMouseOver(mousePos)) {
            return scrollbar.onMouseDWheel(null, null, wheel);
        }
        return false;
    }

}
