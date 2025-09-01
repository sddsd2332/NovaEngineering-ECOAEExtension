package github.kasuminova.ecoaeextension.client.gui.widget.efabricator;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.MultiLineLabel;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.ecoaeextension.client.gui.GuiEFabricatorController;
import github.kasuminova.ecoaeextension.client.gui.widget.ProgressBar;
import github.kasuminova.ecoaeextension.client.gui.widget.SizedColumn;
import github.kasuminova.ecoaeextension.client.gui.widget.SizedRow;
import github.kasuminova.ecoaeextension.client.gui.widget.efabricator.event.EFGUIDataUpdateEvent;
import github.kasuminova.ecoaeextension.common.container.data.EFabricatorData;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorWorker;
import net.minecraft.client.resources.I18n;

import java.util.Collections;

public class StatisticPanel extends SizedRow {

    public static final int WIDTH = 148;
    public static final int HEIGHT = 36;
    public static final TextureProperties TEXTURE_BACKGROUND = new TextureProperties(
            GuiEFabricatorController.TEXTURES_ELEMENTS, 1, 219, WIDTH, HEIGHT
    );

    public StatisticPanel() {
        setWidthHeight(WIDTH, HEIGHT);
        Modules modules = new Modules();
        QueueStatistics queueStatistics = new QueueStatistics();
        ParallelismStatistics parallelismStatistics = new ParallelismStatistics();
        addWidgets(
                modules
                        .setMarginLeft(2)
                        .setMarginUp(2),
                queueStatistics
                        .setMarginLeft(1)
                        .setMarginUp(2),
                parallelismStatistics
                        .setMarginLeft(1)
                        .setMarginUp(2)
        );
    }

    @Override
    protected void preRenderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        TEXTURE_BACKGROUND.render(renderPos, gui);
        super.preRenderInternal(gui, renderSize, renderPos, mousePos);
    }

    public static class Modules extends SizedColumn {

        public static final int WIDTH = 40;
        public static final int HEIGHT = 32;
        public static final TextureProperties TEXTURE_BACKGROUND = new TextureProperties(
                GuiEFabricatorController.TEXTURES_ELEMENTS, 1, 186, WIDTH, HEIGHT
        );

        private final Level level = new Level();
        private final MultiLineLabel patternBusCount;
        private final MultiLineLabel parallelProcCount;
        private final MultiLineLabel workerCount;

        public Modules() {
            setWidthHeight(WIDTH, HEIGHT);
            this.patternBusCount = new MultiLineLabel(Collections.singletonList(I18n.format("gui.efabricator.pattern_bus.count", 0)));
            this.parallelProcCount = new MultiLineLabel(Collections.singletonList(I18n.format("gui.efabricator.parallel_proc.count", 0)));
            this.workerCount = new MultiLineLabel(Collections.singletonList(I18n.format("gui.efabricator.worker.count", 0)));
            addWidgets(
                    level.setMargin(2, 0, 2, 2),
                    patternBusCount
                            .setAutoRecalculateSize(false)
                            .setAutoWrap(false)
                            .setWidth(this.width - 2)
                            .setScale(.6f)
                            .setMargin(1),
                    parallelProcCount
                            .setAutoRecalculateSize(false)
                            .setAutoWrap(false)
                            .setWidth(this.width - 2)
                            .setScale(.6f)
                            .setMargin(1),
                    workerCount
                            .setAutoRecalculateSize(false)
                            .setAutoWrap(false)
                            .setWidth(this.width - 2)
                            .setScale(.6f)
                            .setMargin(1)
            );
        }

        @Override
        public boolean onGuiEvent(final GuiEvent event) {
            if (event instanceof EFGUIDataUpdateEvent efGUIEvent) {
                EFabricatorData data = efGUIEvent.getEFGui().getData();
                if (data == null) {
                    return super.onGuiEvent(event);
                }
                patternBusCount.setContents(Collections.singletonList(
                        I18n.format("gui.efabricator.pattern_bus.count", data.length() * 2))
                );
                parallelProcCount.setContents(Collections.singletonList(
                        I18n.format("gui.efabricator.parallel_proc.count", data.length() * 2))
                );
                workerCount.setContents(Collections.singletonList(
                        I18n.format("gui.efabricator.worker.count", data.length()))
                );
            }
            return super.onGuiEvent(event);
        }

        public Level getLevel() {
            return level;
        }

        @Override
        protected void preRenderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
            TEXTURE_BACKGROUND.render(renderPos, gui);
            super.preRenderInternal(gui, renderSize, renderPos, mousePos);
        }

        public static class Level extends DynamicWidget {

            public static final TextureProperties LEVEL = new TextureProperties(
                    GuiEFabricatorController.TEXTURES_ELEMENTS, 186, 1, 4, 9
            );
            public static final TextureProperties L0 = new TextureProperties(
                    GuiEFabricatorController.TEXTURES_ELEMENTS, 191, 1, 5, 9
            );
            public static final TextureProperties L4 = new TextureProperties(
                    GuiEFabricatorController.TEXTURES_ELEMENTS, 197, 1, 5, 9
            );
            public static final TextureProperties L6 = new TextureProperties(
                    GuiEFabricatorController.TEXTURES_ELEMENTS, 203, 1, 5, 9
            );
            public static final TextureProperties L9 = new TextureProperties(
                    GuiEFabricatorController.TEXTURES_ELEMENTS, 209, 1, 5, 9
            );

            private TextureProperties texture = L0;

            public Level() {
                setWidthHeight(LEVEL.width() + L0.width() + 1, LEVEL.height());
            }

            @Override
            public boolean onGuiEvent(final GuiEvent event) {
                if (event instanceof EFGUIDataUpdateEvent efGUIEvent) {
                    EFabricatorData data = efGUIEvent.getEFGui().getData();
                    if (data == null) {
                        return super.onGuiEvent(event);
                    }
                    switch (data.level()) {
                        case L4 -> texture = L4;
                        case L6 -> texture = L6;
                        case L9 -> texture = L9;
                        default -> texture = L0;
                    }
                }
                return super.onGuiEvent(event);
            }

            @Override
            public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
                LEVEL.render(renderPos, gui);
                texture.renderIfPresent(renderPos.add(new RenderPos(LEVEL.width() + 1 /* padding */, 0)), gui);
            }

            public TextureProperties getTexture() {
                return texture;
            }

            public Level setTexture(final TextureProperties texture) {
                this.texture = texture;
                return this;
            }

        }

    }

    public static class QueueStatistics extends SizedRow {

        public static final int WIDTH = 51;
        public static final int HEIGHT = 32;
        public static final TextureProperties TEXTURE_BACKGROUND = new TextureProperties(
                GuiEFabricatorController.TEXTURES_ELEMENTS, 42, 186, WIDTH, HEIGHT
        );

        private final Info info;
        private final ProgressBar queueStatisticsBar;

        public QueueStatistics() {
            setWidthHeight(WIDTH, HEIGHT);
            this.queueStatisticsBar = new ProgressBar();
            this.info = new Info();
            addWidgets(
                    info
                            .setMargin(2, 0, 2, 0),
                    queueStatisticsBar
                            .setBackgroundTexture(TextureProperties.of(TEXTURE_BACKGROUND.texRes(), 146, 190, 9, 28))
                            .addForegroundTexture(TextureProperties.of(TEXTURE_BACKGROUND.texRes(), 150, 227, 9, 28))
                            .setDownToUp(true)
                            .setMaxProgress(1f)
                            .setWidthHeight(9, 28)
                            .setMargin(0, 0, 2, 0)
            );
        }

        @Override
        public boolean onGuiEvent(final GuiEvent event) {
            if (event instanceof EFGUIDataUpdateEvent efGUIEvent) {
                EFabricatorData data = efGUIEvent.getEFGui().getData();
                if (data == null) {
                    return super.onGuiEvent(event);
                }

                int length = data.length();
                int queueDepth = EFabricatorWorker.MAX_QUEUE_DEPTH;
                if (data.overclocked()) {
                    queueDepth = data.level().applyOverclockQueueDepth(queueDepth);
                }
                int works = data.workers().stream()
                        .mapToInt(EFabricatorData.WorkerStatus::queueLength)
                        .sum();

                queueStatisticsBar.setMaxProgress(length * queueDepth)
                        .setProgress(works);
                info.setProgress(works, length * queueDepth);
            }
            return super.onGuiEvent(event);
        }

        @Override
        protected void preRenderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
            TEXTURE_BACKGROUND.render(renderPos, gui);
            super.preRenderInternal(gui, renderSize, renderPos, mousePos);
        }

        public static class Info extends SizedColumn {

            public static final int WIDTH = 51;
            public static final int HEIGHT = 32;

            private final MultiLineLabel progressPercent;
            private final MultiLineLabel progress;

            public Info() {
                setWidthHeight(WIDTH - (9 + 2 + 2), HEIGHT);
                this.progressPercent = new MultiLineLabel(Collections.singletonList(
                        I18n.format("gui.efabricator.crafting_progress.0", 0)
                ));
                this.progress = new MultiLineLabel(Collections.singletonList(
                        I18n.format("gui.efabricator.crafting_progress.1", 0, 0)
                ));
                addWidgets(
                        progressPercent
                                .setAutoRecalculateSize(false)
                                .setAutoWrap(false)
                                .setWidth(this.width - 2)
                                .setScale(.6f)
                                .setMargin(1),
                        progress
                                .setAutoRecalculateSize(false)
                                .setAutoWrap(false)
                                .setWidth(this.width - 2)
                                .setScale(.6f)
                                .setMargin(1)
                );
            }

            public Info setProgress(int prog, int max) {
                progressPercent.setContents(Collections.singletonList(
                        I18n.format("gui.efabricator.crafting_progress.0",  max <= 0 ? 0 : prog * 100 / max)
                ));
                progress.setContents(Collections.singletonList(
                        I18n.format("gui.efabricator.crafting_progress.1", prog, max)
                ));
                return this;
            }

        }

    }

    public static class ParallelismStatistics extends SizedRow {

        public static final int WIDTH = 51;
        public static final int HEIGHT = 32;
        public static final TextureProperties TEXTURE_BACKGROUND = new TextureProperties(
                GuiEFabricatorController.TEXTURES_ELEMENTS, 94, 186, WIDTH, HEIGHT
        );

        private final Info info;
        private final ProgressBar parallelismBar;

        public ParallelismStatistics() {
            setWidthHeight(WIDTH, HEIGHT);
            this.info = new Info();
            this.parallelismBar = new ProgressBar();
            addWidgets(
                    info
                            .setMargin(2, 0, 2, 0),
                    parallelismBar
                            .setBackgroundTexture(TextureProperties.of(TEXTURE_BACKGROUND.texRes(), 156, 190, 9, 28))
                            .addForegroundTexture(TextureProperties.of(TEXTURE_BACKGROUND.texRes(), 160, 227, 9, 28))
                            .setDownToUp(true)
                            .setMaxProgress(1f)
                            .setWidthHeight(9, 28)
                            .setMargin(0, 0, 2, 0)
            );
        }

        @Override
        public boolean onGuiEvent(final GuiEvent event) {
            if (event instanceof EFGUIDataUpdateEvent efGUIEvent) {
                EFabricatorData data = efGUIEvent.getEFGui().getData();
                if (data == null) {
                    return super.onGuiEvent(event);
                }

                int length = data.length();
                int queueDepth = EFabricatorWorker.MAX_QUEUE_DEPTH;
                if (data.overclocked()) {
                    queueDepth = data.level().applyOverclockQueueDepth(queueDepth);
                }

                int parallelism = data.maxParallelism();

                parallelismBar
                        .setMaxProgress(parallelism)
                        .setProgress(Math.min(parallelism, length * queueDepth));
                info.setParallelism(parallelism, length * queueDepth);
            }
            return super.onGuiEvent(event);
        }

        @Override
        protected void preRenderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
            TEXTURE_BACKGROUND.render(renderPos, gui);
            super.preRenderInternal(gui, renderSize, renderPos, mousePos);
        }

        public static class Info extends SizedColumn {

            public static final int WIDTH = 51;
            public static final int HEIGHT = 32;

            private final MultiLineLabel parallelism;
            private final MultiLineLabel parallelismLimit;
            private final MultiLineLabel parallelismOverflow;

            public Info() {
                setWidthHeight(WIDTH - (9 + 2 + 2), HEIGHT);
                this.parallelism = new MultiLineLabel(Collections.singletonList(
                        I18n.format("gui.efabricator.total_parallelism", 0)
                ));
                this.parallelismLimit = new MultiLineLabel(Collections.singletonList(
                        I18n.format("gui.efabricator.total_parallelism.limit", 0)
                ));
                this.parallelismOverflow = new MultiLineLabel(Collections.singletonList(
                        I18n.format("gui.efabricator.total_parallelism.overflow", 0, 0)
                ));
                addWidgets(
                        parallelism
                                .setAutoRecalculateSize(false)
                                .setAutoWrap(false)
                                .setWidth(this.width - 2)
                                .setScale(.6f)
                                .setMargin(1),
                        parallelismLimit
                                .setAutoRecalculateSize(false)
                                .setAutoWrap(false)
                                .setWidth(this.width - 2)
                                .setScale(.6f)
                                .setMargin(1),
                        parallelismOverflow
                                .setAutoRecalculateSize(false)
                                .setAutoWrap(false)
                                .setWidth(this.width - 2)
                                .setScale(.6f)
                                .setMargin(1)
                );
            }

            public Info setParallelism(int parallelism, int parallelismLimit) {
                this.parallelism.setContents(Collections.singletonList(
                        I18n.format("gui.efabricator.total_parallelism", parallelism)
                ));
                this.parallelismLimit.setContents(Collections.singletonList(
                        I18n.format("gui.efabricator.total_parallelism.limit", parallelismLimit)
                ));
                int overflow = Math.max(parallelism - parallelismLimit, 0);
                this.parallelismOverflow.setContents(Collections.singletonList(
                        I18n.format("gui.efabricator.total_parallelism.overflow", overflow, overflow == 0 ? 0 : (overflow * 100 / parallelism))
                ));
                return this;
            }

        }

    }

}
