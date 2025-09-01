package github.kasuminova.ecoaeextension.client.gui.widget.ecalculator;

import appeng.api.storage.data.IAEItemStack;
import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.container.Row;
import github.kasuminova.mmce.client.gui.widget.container.ScrollingColumn;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.mmce.client.gui.widget.slot.SlotItemVirtualJEI;
import github.kasuminova.ecoaeextension.client.gui.GuiECalculatorController;
import github.kasuminova.ecoaeextension.client.gui.widget.SizedRow;
import github.kasuminova.ecoaeextension.client.gui.widget.ecalculator.event.ECGUIDataUpdateEvent;
import github.kasuminova.ecoaeextension.common.container.data.ECalculatorData;
import github.kasuminova.ecoaeextension.common.crafttweaker.util.NovaEngUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class MonitorPanel extends SizedRow {

    private static final int WIDTH = 241;
    private static final int HEIGHT = 77;

    private static final int OFFSET_X = 1;
    private static final int OFFSET_Y = 1;

    private static final TextureProperties BACKGROUND = new TextureProperties(
            GuiECalculatorController.ELEMENT_1,
            0, 129,
            WIDTH, HEIGHT
    );

    public MonitorPanel() {
        setWidthHeight(WIDTH, HEIGHT);
        addWidgets(
                new DataPanel().setMarginLeft(3).setMarginUp(3),
                new TaskPanel().setUseScissor(true).setMarginLeft(5).setMarginUp(5)
        );
    }

    @Override
    protected void renderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        BACKGROUND.render(renderPos, gui);
        super.renderInternal(gui, renderSize, renderPos, mousePos);
    }

    public static class DataPanel extends DynamicWidget {

        private static final int WIDTH = 84;
        private static final int HEIGHT = 71;

        private long totalStorage;
        private long usedMemory;
        private long usedExtraStorage;
        private int threads;
        private int hyperThreads;
        private int maxThreads;
        private int maxHyperThreads;
        private int cpuUsage;
        private int energyUsage;
        private int totalParallelism;

        public DataPanel() {
            setWidthHeight(WIDTH, HEIGHT);
        }

        @Override
        public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(renderPos.posX() + 2, renderPos.posY() + 2, 0);
            {
                final FontRenderer fr = gui.getGui().mc.fontRenderer;

                // Used Memory
                GlStateManager.pushMatrix();
                GlStateManager.translate(4, 4, 0);
                {
                    final int length = 32;
                    String memStr = NovaEngUtils.formatNumber(this.usedMemory) + 'B';
                    if (memStr.length() > 6) {
                        memStr = NovaEngUtils.formatNumber(this.usedMemory, 0);
                    }
                    final String usedMemory = TextFormatting.YELLOW + "U:" + memStr;
                    memStr = NovaEngUtils.formatNumber(this.usedExtraStorage) + 'B';
                    if (memStr.length() > 6) {
                        memStr = NovaEngUtils.formatNumber(this.usedExtraStorage, 0);
                    }
                    final String usedExtraMemory = TextFormatting.RED + "E:" + memStr + TextFormatting.RESET;
                    int offsetX = length - fr.getStringWidth(usedMemory);
                    fr.drawStringWithShadow(usedMemory, offsetX, 0, 0xFFFFFF);
                    offsetX = length - fr.getStringWidth(usedExtraMemory);
                    fr.drawStringWithShadow(usedExtraMemory, offsetX, 10, 0xFFFFFF);
                }
                GlStateManager.popMatrix();

                // Total Memory
                GlStateManager.pushMatrix();
                GlStateManager.translate(44, 4, 0);
                {
                    String memStr = NovaEngUtils.formatNumber(this.totalStorage) + 'B';
                    if (memStr.length() > 6) {
                        memStr = NovaEngUtils.formatNumber(this.totalStorage, 0);
                    }
                    final String totalMemory = TextFormatting.GREEN + "T:" + memStr;
                    fr.drawStringWithShadow(totalMemory, 0, 0, 0xFFFFFF);
                    final String percentage = TextFormatting.GRAY + NovaEngUtils.formatPercent(this.usedMemory + this.usedExtraStorage, this.totalStorage);
                    fr.drawStringWithShadow(percentage, 0, 10, 0xFFFFFF);
                }
                GlStateManager.popMatrix();

                // Threads
                GlStateManager.pushMatrix();
                GlStateManager.translate(13, 30, 0);
                {
                    final String threads = String.valueOf(this.threads);
                    final String hyperThreads = String.valueOf(this.hyperThreads);
                    final String maxThreads = String.valueOf(this.maxThreads);
                    final String maxHyperThreads = String.valueOf(this.maxHyperThreads);
                    final String formatted = String.format("%s (%s%s%s) / %s (%s%s%s)",
                            threads,
                            TextFormatting.RED, hyperThreads, TextFormatting.RESET,
                            maxThreads,
                            TextFormatting.YELLOW, maxHyperThreads, TextFormatting.RESET
                    );

                    fr.drawStringWithShadow(formatted, 0, 0, 0xFFFFFF);
                }
                GlStateManager.popMatrix();

                // CPU Usage
                GlStateManager.pushMatrix();
                GlStateManager.translate(13, 43, 0);
                {
                    final String cpuUsage = String.format("%d µs/t", this.cpuUsage);
                    fr.drawStringWithShadow(cpuUsage, 0, 0, 0xFFFFFF);
                }
                GlStateManager.popMatrix();

                // Energy
                GlStateManager.pushMatrix();
                GlStateManager.translate(13, 56 + 1, 0);
                GlStateManager.scale(.7F, .7F, .7F);
                {
                    final String energyUsage = String.format("%s AE/t", NovaEngUtils.formatNumber(this.energyUsage));
                    fr.drawStringWithShadow(energyUsage, 0, 0, 0xFFFFFF);
                }
                GlStateManager.popMatrix();

                // Total Parallelism
                GlStateManager.pushMatrix();
                if (this.totalParallelism >= 100_000) {
                    GlStateManager.translate(53, 56 + 1, 0);
                    GlStateManager.scale(.8F, .8F, .8F);
                } else {
                    GlStateManager.translate(53, 56, 0);
                }
                {
                    final String totalParallelism = String.valueOf(this.totalParallelism);
                    fr.drawStringWithShadow(totalParallelism, 0, 0, 0xFFFFFF);
                }
                GlStateManager.popMatrix();
            }
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.popMatrix();
        }

        @Override
        public boolean onGuiEvent(final GuiEvent event) {
            if (event instanceof ECGUIDataUpdateEvent ecGUIEvent) {
                GuiECalculatorController ecGui = ecGUIEvent.getECGui();
                ECalculatorData data = ecGui.getData();
                long totalStorage = data.totalStorage();
                long usedExtraStorage = data.usedExtraStorage();
                long usedTotalMemory = data.ecpuList().stream().mapToLong(ecpuData -> ecpuData.usedMemory() - ecpuData.usedExtraMemory()).sum();
                this.totalStorage = totalStorage;
                this.usedMemory = usedTotalMemory;
                this.usedExtraStorage = usedExtraStorage;
                this.hyperThreads = (int) data.ecpuList().stream().filter(ecpuData -> ecpuData.usedExtraMemory() > 0).count();
                this.threads = data.ecpuList().size() - hyperThreads;
                this.maxThreads = data.threadCores().stream().mapToInt(ECalculatorData.ThreadCoreData::maxThreads).sum();
                this.maxHyperThreads = data.threadCores().stream().mapToInt(ECalculatorData.ThreadCoreData::maxHyperThreads).sum();
                this.cpuUsage = data.cpuUsagePerSecond();
                this.energyUsage = 0;
                this.totalParallelism = data.accelerators();
            }
            return super.onGuiEvent(event);
        }

    }

    public static class TaskPanel extends ScrollingColumn {

        private static final int WIDTH = 144;
        private static final int HEIGHT = 67;

        public TaskPanel() {
            setWidthHeight(WIDTH, HEIGHT);
        }

        @Override
        public void update(final WidgetGui gui) {
            super.update(gui);
            scrollbar.setScrollUnit(scrollbar.getRange() / 8);
        }

        @Override
        public void initWidget(final WidgetGui gui) {
            super.initWidget(gui);
            scrollbar.setMargin(0, 0, 1, 1);
            scrollbar.setWidthHeight(5, 65);

            final TextureProperties scrollTexture = TextureProperties.of(207, 208);
            scrollbar.getScroll()
                    .setMouseDownTexture(scrollTexture)
                    .setUnavailableTexture(scrollTexture)
                    .setHoveredTexture(scrollTexture)
                    .setTexture(scrollTexture)
                    .setTextureLocation(GuiECalculatorController.ELEMENT_1)
                    .setWidthHeight(5, 15);
        }

        @Override
        public boolean onGuiEvent(final GuiEvent event) {
            if (event instanceof ECGUIDataUpdateEvent ecGUIEvent) {
                final GuiECalculatorController ecGui = ecGUIEvent.getECGui();
                final ECalculatorData data = ecGui.getData();
                final List<DynamicWidget> widgets = getWidgets();
                final int maxElementPerRow = 2;

                widgets.clear();
                int element = 0;
                Row row = new Row();
                for (final ECalculatorData.ECPUData ecpuData : data.ecpuList()) {
                    row.addWidget(new Task(ecpuData));
                    element++;
                    if (element >= maxElementPerRow) {
                        widgets.add(row.setUseScissor(false));
                        row = new Row();
                        element = 0;
                    }
                }
                row.getWidgets().forEach(widget -> widget.setMarginDown(3));
                widgets.add(row.setUseScissor(false));
            }
            return super.onGuiEvent(event);
        }

        public static class Task extends SizedRow {

            private static final int WIDTH = 65;
            private static final int HEIGHT = 26;

            private static final int ITEM_OFFSET_X = 3;
            private static final int ITEM_OFFSET_Y = 4;

            private static final TextureProperties BACKGROUND = new TextureProperties(
                    GuiECalculatorController.ELEMENT_1, 1, 208, WIDTH, HEIGHT
            );
            private static final TextureProperties MEMORY_ICON = new TextureProperties(
                    GuiECalculatorController.ELEMENT_1, 215, 208, 7, 7
            );
            private static final TextureProperties PARALLELISM_ICON = new TextureProperties(
                    GuiECalculatorController.ELEMENT_1, 215, 218, 7, 7
            );

            private final long usedMemory;
            private final int parallelismPreSecond;
            private final int cpuUsage;

            public Task(final ECalculatorData.ECPUData ecpuData) {
                setWidthHeight(WIDTH, HEIGHT);
                setUseScissor(false);
                setMargin(2, 0, 2, 0);
                final IAEItemStack crafting = ecpuData.crafting();
                this.usedMemory = ecpuData.usedMemory();
                this.parallelismPreSecond = ecpuData.parallelismPreSecond();
                this.cpuUsage = ecpuData.cpuUsagePerSecond();
                addWidget(new SlotItemVirtualJEI(crafting.createItemStack()).setAbsXY(ITEM_OFFSET_X, ITEM_OFFSET_Y));
            }

            @Override
            protected void renderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
                BACKGROUND.render(renderPos, gui);
                super.renderInternal(gui, renderSize, renderPos, mousePos);
                final String memory = NovaEngUtils.formatNumber(usedMemory) + "B";
                final String parallelism = parallelismPreSecond + "/t";
                final String cpuUsage = this.cpuUsage + "µs/t";

                final FontRenderer fr = gui.getGui().mc.fontRenderer;
                final float width = 38 / .8F;
                GlStateManager.pushMatrix();
                GlStateManager.translate(renderPos.posX() + 24, renderPos.posY() + 2, 0);
                {
                    MEMORY_ICON.render(new RenderPos(0, 0), gui);
                    PARALLELISM_ICON.render(new RenderPos(0, 8), gui);

                    GlStateManager.pushMatrix();
                    GlStateManager.scale(.8F, .8F, .8F);
                    {
                        float strWidth = fr.getStringWidth(memory);
                        fr.drawStringWithShadow(memory, width - strWidth, 0, 0xFFFFFFFF);
                        strWidth = fr.getStringWidth(parallelism);
                        fr.drawStringWithShadow(parallelism, width - strWidth, 10, 0xFFFFFFFF);
                        strWidth = fr.getStringWidth(cpuUsage);
                        fr.drawStringWithShadow(cpuUsage, width - strWidth, 19, 0xFFFFFFFF);
                    }
                    GlStateManager.popMatrix();
                }
                GlStateManager.popMatrix();
            }

        }

    }

}
