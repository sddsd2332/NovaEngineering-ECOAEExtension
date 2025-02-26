package github.kasuminova.ecoaeextension.client.gui.widget.ecalculator;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.ecoaeextension.client.gui.GuiECalculatorController;
import github.kasuminova.ecoaeextension.client.gui.widget.ecalculator.event.ECGUIDataUpdateEvent;
import github.kasuminova.ecoaeextension.common.container.data.ECalculatorData;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Comparator;

public class StorageBar extends DynamicWidget {

    private static final int WIDTH = 241;
    private static final int HEIGHT = 50;

    private static final int LINE_WIDTH = 233;
    private static final int LINE_HEIGHT = 42;

    private static final int OFFSET_X = 4;
    private static final int OFFSET_Y = 4;

    private static final TextureProperties BACKGROUND = new TextureProperties(
            GuiECalculatorController.ELEMENT_1,
            0, 0,
            WIDTH, HEIGHT
    );

    private final LongList usedMemory = new LongArrayList();
    private long totalStorage = 0;

    public StorageBar() {
        setWidthHeight(WIDTH, HEIGHT);
    }

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        BACKGROUND.render(renderPos, gui);
        if (this.usedMemory.isEmpty()) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(renderPos.posX() + OFFSET_X, renderPos.posY() + OFFSET_Y, 0);
        {
            int total = 0;
            int color = 0x7F87CEFA;
            int swapColor = 0x7F6495ED;
            int tmp;
            LongListIterator it = this.usedMemory.iterator();
            while (it.hasNext()) {
                long usedMemory = it.nextLong();
                float percent = (float) usedMemory / totalStorage;
                int width = (int) Math.min(Math.max(LINE_WIDTH * percent, 3), LINE_WIDTH);

                // Body
                Gui.drawRect(0, 1, width - 1, LINE_HEIGHT - 1, color);
                // Up
                Gui.drawRect(0, 0, width, 1, color | 0xFF000000);
                // Down
                Gui.drawRect(0, LINE_HEIGHT - 1, width, LINE_HEIGHT, color | 0xFF000000);
                // Left
                Gui.drawRect(0, 0, 1, LINE_HEIGHT, color | 0xFF000000);
                // Right
                Gui.drawRect(width - 1, 0, width, LINE_HEIGHT, color | 0xFF000000);

                total += width;
                if (total >= LINE_WIDTH || !it.hasNext()) {
                    break;
                }

                GlStateManager.translate(width, 0, 0);
                tmp = color;
                color = swapColor;
                swapColor = tmp;
            }
            GlStateManager.enableBlend();
            GlStateManager.color(1F, 1F, 1F, 1F);
        }
        GlStateManager.popMatrix();

        int centerX = renderPos.posX() + OFFSET_X + (renderSize.width() / 2);
        int centerY = renderPos.posY() + OFFSET_Y + (renderSize.height() / 2);
    }

    @Override
    public boolean onGuiEvent(final GuiEvent event) {
        if (event instanceof ECGUIDataUpdateEvent ecGuiEvent) {
            GuiECalculatorController ecGui = ecGuiEvent.getECGui();
            ECalculatorData data = ecGui.getData();
            this.totalStorage = data.totalStorage();
            this.usedMemory.clear();
            for (final ECalculatorData.ECPUData ecpuData : data.ecpuList()) {
                this.usedMemory.add(ecpuData.usedMemory());
            }
            this.usedMemory.sort(Comparator.comparingLong(value -> (long) value).reversed());
        }
        return super.onGuiEvent(event);
    }

}
