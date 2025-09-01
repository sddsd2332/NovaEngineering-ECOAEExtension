package github.kasuminova.ecoaeextension.client.gui.widget.efabricator;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.MultiLineLabel;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.container.Column;
import github.kasuminova.mmce.client.gui.widget.container.Row;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.ecoaeextension.client.gui.GuiEFabricatorController;
import github.kasuminova.ecoaeextension.client.gui.widget.ProgressBar;
import github.kasuminova.ecoaeextension.client.gui.widget.SizedColumn;
import github.kasuminova.ecoaeextension.client.gui.widget.efabricator.event.EFGUIDataUpdateEvent;
import github.kasuminova.ecoaeextension.common.container.data.EFabricatorData;
import github.kasuminova.ecoaeextension.common.crafttweaker.util.NovaEngUtils;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorWorker;
import net.minecraft.client.resources.I18n;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class HeatStatisticPanel extends SizedColumn {

    public static final int WIDTH = 51;
    public static final int HEIGHT = 77;
    public static final TextureProperties TEXTURE_BACKGROUND = new TextureProperties(
            GuiEFabricatorController.TEXTURES_ELEMENTS, 204, 24, WIDTH, HEIGHT
    );

    private final BackgroundLabel energyUsage;
    private final BackgroundLabel activeCooling;
    private final ProgressBar energyUsageBar;
    private final ProgressBar coolantBar;
    private final ProgressBar hotCoolantBar;

    public HeatStatisticPanel() {
        setWidthHeight(WIDTH, HEIGHT);

        energyUsage = new BackgroundLabel(Collections.singletonList(I18n.format("gui.efabricator.max_energy_usage", 0)));
        energyUsage
                .setTooltipFunction(label -> Collections.singletonList(I18n.format("gui.efabricator.max_energy_usage.tip")))
                .setBackground(TextureProperties.of(TEXTURE_BACKGROUND.texRes(), 210, 102, 45, 9))
                .setPadding(1);
        energyUsage.getLabel()
                .setAutoRecalculateSize(false)
                .setAutoWrap(false)
                .setVerticalCentering(true)
                .setScale(0.6f)
                .setWidthHeight(43, 6)
                .setMarginDown(2);
        activeCooling = new BackgroundLabel(Collections.singletonList(I18n.format(
                "gui.efabricator.active_cooling", I18n.format("gui.efabricator.active_cooling.off")
        )));
        activeCooling
                .setBackground(TextureProperties.of(TEXTURE_BACKGROUND.texRes(), 210, 102, 45, 9))
                .setPadding(1);
        activeCooling.getLabel()
                .setAutoRecalculateSize(false)
                .setAutoWrap(false)
                .setVerticalCentering(true)
                .setScale(0.75f)
                .setWidthHeight(43, 7);

        energyUsageBar = new ProgressBar();
        coolantBar = new ProgressBar();
        hotCoolantBar = new ProgressBar();
        addWidgets(
                energyUsage
                        .setWidthHeight(45, 9)
                        .setMargin(3, 0, 3, 0),
                energyUsageBar
                        .setBackgroundTexture(TextureProperties.of(TEXTURE_BACKGROUND.texRes(), 210, 122, 45, 9))
                        .addForegroundTexture(TextureProperties.of(TEXTURE_BACKGROUND.texRes(), 210, 112, 45, 9))
                        .setLeftToRight(true)
                        .setMaxProgress(1f)
                        .setWidthHeight(45, 9)
                        .setMargin(3, 0, 3, 0),
                activeCooling
                        .setWidthHeight(45, 9)
                        .setMargin(3, 0, 3, 0),
                new Row()
                        .addWidgets(
                                coolantBar
                                        .setBackgroundTexture(TextureProperties.of(TEXTURE_BACKGROUND.texRes(), 212, 134, 21, 36))
                                        .addForegroundTexture(TextureProperties.of(TEXTURE_BACKGROUND.texRes(), 108, 149, 21, 36))
                                        .setDownToUp(true)
                                        .setMaxProgress(1f)
                                        .setWidthHeight(21, 36)
                                        .setMarginRight(2),
                                hotCoolantBar
                                        .setBackgroundTexture(TextureProperties.of(TEXTURE_BACKGROUND.texRes(), 234, 134, 21, 36))
                                        .addForegroundTexture(TextureProperties.of(TEXTURE_BACKGROUND.texRes(), 130, 149, 21, 36))
                                        .setDownToUp(true)
                                        .setMaxProgress(1f)
                                        .setWidthHeight(21, 36)
                        )
                        .setMargin(3, 0, 3, 0)
        );
    }

    @Override
    public boolean onGuiEvent(final GuiEvent event) {
        if (event instanceof EFGUIDataUpdateEvent efGUIEvent) {
            EFabricatorData data = efGUIEvent.getEFGui().getData();
            if (data == null) {
                return super.onGuiEvent(event);
            }

            this.activeCooling.getLabel()
                    .setContents(Collections.singletonList(
                            I18n.format("gui.efabricator.active_cooling", data.activeCooling()
                                    ? I18n.format("gui.efabricator.active_cooling.on")
                                    : I18n.format("gui.efabricator.active_cooling.off")
                            )
                    ));

            int length = data.length();
            int queueDepth = EFabricatorWorker.MAX_QUEUE_DEPTH;
            if (data.overclocked()) {
                queueDepth = data.level().applyOverclockQueueDepth(queueDepth);
            }

            int maxEnergy = EFabricatorWorker.MAX_ENERGY_CACHE * length;
            this.energyUsageBar.setMaxProgress(maxEnergy)
                    .setProgress(data.energyStored());

            int energyUsage = EFabricatorWorker.ENERGY_USAGE * length * queueDepth;
            if (data.overclocked() && !data.activeCooling()) {
                energyUsage = data.level().applyOverclockEnergyUsage(energyUsage);
            }
            this.energyUsage.getLabel()
                    .setContents(Collections.singletonList(
                            I18n.format("gui.efabricator.max_energy_usage", NovaEngUtils.formatNumber(energyUsage, 1))
                    ));

            this.coolantBar
                    .setMaxProgress(data.maxCoolant())
                    .setProgress(data.coolant());
            this.hotCoolantBar
                    .setMaxProgress(data.maxHotCoolant())
                    .setProgress(data.hotCoolant());
        }
        return super.onGuiEvent(event);
    }

    @Override
    protected void preRenderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        TEXTURE_BACKGROUND.render(renderPos, gui);
        super.preRenderInternal(gui, renderSize, renderPos, mousePos);
    }

    public static class BackgroundLabel extends Column {

        private final MultiLineLabel label;
        private TextureProperties background = TextureProperties.EMPTY;
        private Function<BackgroundLabel, List<String>> tooltipFunction = null;

        public BackgroundLabel(final List<String> contents) {
            this.label = new MultiLineLabel(contents);
            addWidget(label);
        }

        @Override
        protected void preRenderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
            background.renderIfPresent(renderPos, gui);
            super.preRenderInternal(gui, renderSize, renderPos, mousePos);
        }

        public TextureProperties getBackground() {
            return background;
        }

        public BackgroundLabel setBackground(final TextureProperties background) {
            this.background = background;
            return this;
        }

        public MultiLineLabel getLabel() {
            return label;
        }

        public BackgroundLabel setPadding(final int padding) {
            label.setMargin(padding, padding, padding, padding);
            return this;
        }

        @Override
        public List<String> getHoverTooltips(final WidgetGui widgetGui, final MousePos mousePos) {
            return tooltipFunction != null ? tooltipFunction.apply(this) : Collections.emptyList();
        }

        public BackgroundLabel setTooltipFunction(final Function<BackgroundLabel, List<String>> tooltipFunction) {
            this.tooltipFunction = tooltipFunction;
            return this;
        }

    }

}
