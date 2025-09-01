package github.kasuminova.ecoaeextension.client.gui.widget.ecalculator;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.container.Row;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.client.gui.GuiECalculatorController;
import github.kasuminova.ecoaeextension.client.gui.widget.ProgressBar;
import github.kasuminova.ecoaeextension.client.gui.widget.SizedColumn;
import github.kasuminova.ecoaeextension.client.gui.widget.SizedRow;
import github.kasuminova.ecoaeextension.client.gui.widget.ecalculator.event.ECGUIDataUpdateEvent;
import github.kasuminova.ecoaeextension.common.container.data.ECalculatorData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class CPUStatusPanel extends SizedColumn {

    private static final int WIDTH = 241;
    private static final int HEIGHT = 77;

    private static final int OFFSET_X = 3;
    private static final int OFFSET_Y = 3;

    private static final TextureProperties BACKGROUND = new TextureProperties(
            GuiECalculatorController.ELEMENT_1,
            0, 51,
            WIDTH, HEIGHT
    );

    public CPUStatusPanel() {
        setWidthHeight(WIDTH, HEIGHT);
    }

    @Override
    protected void renderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        BACKGROUND.render(renderPos, gui);
        super.renderInternal(gui, renderSize, renderPos.add(new RenderPos(OFFSET_X, OFFSET_Y)), mousePos);
    }

    @Override
    protected void preRenderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        super.preRenderInternal(gui, renderSize, renderPos.add(new RenderPos(OFFSET_X, OFFSET_Y)), mousePos);
    }

    @Override
    protected void postRenderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        super.postRenderInternal(gui, renderSize, renderPos.add(new RenderPos(OFFSET_X, OFFSET_Y)), mousePos);
    }

    @Override
    public boolean onGuiEvent(final GuiEvent event) {
        if (event instanceof ECGUIDataUpdateEvent ecGuiEvent) {
            final GuiECalculatorController ecGUI = ecGuiEvent.getECGui();
            final ECalculatorData data = ecGUI.getData();
            final List<ECalculatorData.ThreadCoreData> threadCores = data.threadCores();
            final List<DynamicWidget> widgets = getWidgets();

            widgets.clear();
            final int maxElementPreRow = 6;
            int element = 0;
            Row row = new Row();
            for (final ECalculatorData.ThreadCoreData core : threadCores) {
                CPUStatus status = new CPUStatus(core);
                row.addWidget(status.setMargin(1, 0, 1, 0));
                element++;
                if (element >= maxElementPreRow) {
                    element = 0;
                    widgets.add(row);
                    row = new Row();
                }
            }
            widgets.add(row);
        }
        return super.onGuiEvent(event);
    }

    public static class CPUStatus extends SizedRow {

        private static final int WIDTH = 38;
        private static final int HEIGHT = 34;

        private static final int TEXT_OFFSET_X = 5;
        private static final int TEXT_OFFSET_Y = 24;

        private static final ResourceLocation L4 = new ResourceLocation(ECOAEExtension.MOD_ID,
                "blocks/ec_modular_synthetic_memory/bloom/thread_module/{state}/thread_module");
        private static final ResourceLocation L4_HYPER = new ResourceLocation(ECOAEExtension.MOD_ID,
                "blocks/ec_modular_synthetic_memory/bloom/thread_module/{state}/hyper_threading_module");

        private static final ResourceLocation L6 = new ResourceLocation(ECOAEExtension.MOD_ID,
                "blocks/ec_modular_synthetic_memory/bloom/thread_module/{state}/l6_thread_module");
        private static final ResourceLocation L6_HYPER = new ResourceLocation(ECOAEExtension.MOD_ID,
                "blocks/ec_modular_synthetic_memory/bloom/thread_module/{state}/l6_hyper_threading_module");

        private static final ResourceLocation L9 = new ResourceLocation(ECOAEExtension.MOD_ID,
                "blocks/ec_modular_synthetic_memory/bloom/thread_module/{state}/l9_thread_module");
        private static final ResourceLocation L9_HYPER = new ResourceLocation(ECOAEExtension.MOD_ID,
                "blocks/ec_modular_synthetic_memory/bloom/thread_module/{state}/l9_hyper_threading_module");

        private static final TextureProperties BACKGROUND = new TextureProperties(
                GuiECalculatorController.ELEMENT_2, 70, 0, WIDTH, HEIGHT
        );
        private static final TextureProperties BACKGROUND_OVERLAY = new TextureProperties(
                GuiECalculatorController.ELEMENT_2, 70, 35, WIDTH, HEIGHT
        );
        private static final TextureProperties BACKGROUND_OVERLAY_HYPER = new TextureProperties(
                GuiECalculatorController.ELEMENT_2, 109, 35, WIDTH, HEIGHT
        );

        private final TextureAtlasSprite overlay;
        private final boolean hyper;
        private final int cpus;
        private final int maxThreads;
        private final int maxHyperThreads;
        private final ProgressBar bar;

        public CPUStatus(final ECalculatorData.ThreadCoreData core) {
            setWidthHeight(WIDTH, HEIGHT);
            final boolean hyper = core.maxHyperThreads() > 0;
            final boolean working = core.threads() > 0;
            this.hyper = hyper;
            this.cpus = core.threads() + core.hyperThreads();
            this.maxThreads = core.maxThreads();
            this.maxHyperThreads = core.maxHyperThreads();
            switch (core.type()) {
                case L4 -> overlay = hyper ? getOverlaySprite(L4_HYPER, working) : getOverlaySprite(L4, working);
                case L6 -> overlay = hyper ? getOverlaySprite(L6_HYPER, working) : getOverlaySprite(L6, working);
                case L9 -> overlay = hyper ? getOverlaySprite(L9_HYPER, working) : getOverlaySprite(L9, working);
                default -> overlay = null;
            }
            this.bar = new ProgressBar();
            this.bar.addForegroundTexture(TextureProperties.of(GuiECalculatorController.ELEMENT_2, 148, 0, 7, 28))
                    .setDownToUp(true)
                    .setShouldUseAnimation(false)
                    .setMaxProgress(this.maxThreads + this.maxHyperThreads)
                    .setProgress(this.cpus)
                    .setWidthHeight(7, 28);
            addWidget(bar.setAbsXY(28, 3));
        }

        private static TextureAtlasSprite getOverlaySprite(final ResourceLocation rl, final boolean working) {
            final TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
            final String loc = rl.toString().replace("{state}", working ? "run" : "on") + (working ? "" : "_on");
            return textureMap.getAtlasSprite(loc);
        }

        @Override
        protected void renderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
            BACKGROUND.render(renderPos, gui);
            if (this.hyper) {
                BACKGROUND_OVERLAY_HYPER.render(renderPos, gui);
            } else {
                BACKGROUND_OVERLAY.render(renderPos, gui);
            }
            if (this.overlay != null) {
                TextureManager manager = gui.getGui().mc.getTextureManager();
                manager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                final int posX = renderPos.posX() + 5;
                final int posY = renderPos.posY() + 5;
                gui.getGui().drawTexturedModalRect(posX, posY, this.overlay, 16, 16);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(renderPos.posX() + TEXT_OFFSET_X, renderPos.posY() + TEXT_OFFSET_Y, 0);
            {
                GlStateManager.pushMatrix();
                GlStateManager.scale(.6F, .6F, .6F);
                {
                    final FontRenderer fr = gui.getGui().mc.fontRenderer;
                    final String text;
                    if (this.hyper) {
                        text = String.format("§b%d§r / §9%d§r (§e+%d§r)", cpus, maxThreads, maxHyperThreads);
                    } else {
                        text = String.format("§b%d§r / §9%d§r", cpus, maxThreads);
                    }
                    fr.drawStringWithShadow(text, 0, 0, 0xFFFFFFFF);
                    GlStateManager.color(1F, 1F, 1F, 1F);
                }
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();

            super.renderInternal(gui, renderSize, renderPos, mousePos);
        }

    }

}