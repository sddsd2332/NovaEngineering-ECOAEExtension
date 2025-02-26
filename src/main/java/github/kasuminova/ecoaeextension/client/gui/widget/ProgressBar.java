package github.kasuminova.ecoaeextension.client.gui.widget;

import github.kasuminova.mmce.client.gui.util.*;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ProgressBar extends DynamicWidget {

    protected boolean leftToRight = true;
    protected boolean vertical = false;

    protected TextureProperties backgroundTexture = null;

    protected List<TextureProperties> progressTextures = new ArrayList<>();

    protected AnimationValue progress = AnimationValue.ofFinished(0, 500, .25, .1, .25, 1);
    protected double maxProgress = 1;
    protected boolean shouldUseAnimation = true;

    protected Function<ProgressBar, List<String>> tooltipFunction = null;

    @Override
    public void render(final WidgetGui widgetGui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        GuiScreen gui = widgetGui.getGui();

        // Background.
        if (backgroundTexture != null) {
            backgroundTexture.render(renderPos, gui);
        }

        // ProgressBar / Foreground.
        if (progressTextures.isEmpty() || maxProgress <= 0) {
            return;
        }

        double percent = Math.min(1, progress.get() / maxProgress);

        // Progress background
        findProgressBackgroundTex(percent).ifPresent(tex -> tex.render(renderPos, gui));

        float renderPercent = (float) percent;
        if (isHorizontal()) {
            // Horizontal.
            if (leftToRight) {
                // Left to right.
                findProgressForegroundTex(percent).ifPresent(tex -> {
                    tex.bind(gui.mc.getTextureManager());
                    gui.drawTexturedModalRect(
                            renderPos.posX(), renderPos.posY(),
                            tex.texX(), tex.texY(),
                            (int) (tex.width() * renderPercent), tex.height()
                    );
                });
            } else {
                // Right to left.
                findProgressForegroundTex(percent).ifPresent(tex -> {
                    tex.bind(gui.mc.getTextureManager());
                    gui.drawTexturedModalRect(
                            renderPos.posX() + (int) (tex.width() * (1 - renderPercent)), renderPos.posY(),
                            tex.texX(), tex.texY(),
                            (int) (tex.width() * renderPercent), tex.height()
                    );
                });
            }
        } else {
            // Vertical.
            if (leftToRight) {
                // Down to up.
                findProgressForegroundTex(percent).ifPresent(tex -> {
                    tex.bind(gui.mc.getTextureManager());
                    gui.drawTexturedModalRect(
                            renderPos.posX(), renderPos.posY(),
                            tex.texX(), tex.texY(),
                            tex.width(), (int) (tex.height() * renderPercent)
                    );
                });
            } else {
                // Up to down.
                findProgressForegroundTex(percent).ifPresent(tex -> {
                    tex.bind(gui.mc.getTextureManager());
                    gui.drawTexturedModalRect(
                            renderPos.posX(), renderPos.posY() + (int) (tex.height() * (1 - renderPercent)),
                            tex.texX(), tex.texY() + (int) (tex.height() * (1 - renderPercent)),
                            tex.width(), (int) (tex.height() * renderPercent)
                    );
                });
            }
        }
    }

    protected Optional<TextureProperties> findProgressForegroundTex(final double percent) {
        if (progressTextures.isEmpty()) {
            return Optional.empty();
        }

        int idx = (int) percent;
        if (idx >= progressTextures.size()) {
            return Optional.of(progressTextures.get(progressTextures.size() - 1));
        } else {
            return Optional.of(progressTextures.get(idx));
        }
    }

    protected Optional<TextureProperties> findProgressBackgroundTex(final double percent) {
        if (progressTextures.isEmpty()) {
            return Optional.empty();
        }

        int idx = ((int) percent) - 1;
        if (idx <= 0) {
            return Optional.empty();
        } else if (idx >= progressTextures.size()) {
            return Optional.empty();
        }
        return Optional.of(progressTextures.get(idx));
    }

    // Progress settings.

    public double getProgressPercent() {
        if (maxProgress <= 0) {
            return 0;
        }
        return progress.get() / maxProgress;
    }

    public double getProgress() {
        return progress.get();
    }

    public ProgressBar setProgress(final double progress) {
        if (shouldUseAnimation) {
            this.progress.set(progress);
        } else {
            this.progress.setImmediate(progress);
        }
        return this;
    }

    public double getMaxProgress() {
        return maxProgress;
    }

    public ProgressBar setMaxProgress(final float maxProgress) {
        this.maxProgress = maxProgress;
        if (progress.getTargetValue() > maxProgress) {
            if (shouldUseAnimation) {
                progress.set(maxProgress);
            } else {
                progress.setImmediate(maxProgress);
            }
        }
        return this;
    }

    public boolean isShouldUseAnimation() {
        return shouldUseAnimation;
    }

    public ProgressBar setShouldUseAnimation(final boolean shouldUseAnimation) {
        this.shouldUseAnimation = shouldUseAnimation;
        if (!shouldUseAnimation && !progress.isAnimFinished()) {
            progress.setImmediate(progress.getTargetValue());
        }
        return this;
    }

    // Vertical & Horizontal settings.

    public ProgressBar setVertical(final boolean vertical) {
        this.vertical = vertical;
        return this;
    }

    public boolean isVertical() {
        return vertical;
    }

    public ProgressBar setHorizontal(final boolean horizontal) {
        this.vertical = !horizontal;
        return this;
    }

    // Left to right & Up to down settings.

    public boolean isHorizontal() {
        return !vertical;
    }

    public boolean isLeftToRight() {
        return leftToRight;
    }

    public ProgressBar setLeftToRight(final boolean leftToRight) {
        this.leftToRight = leftToRight;
        return setHorizontal(true);
    }

    public ProgressBar setRightToLeft() {
        return setLeftToRight(!leftToRight);
    }

    public boolean isRightToLeft() {
        return !leftToRight;
    }
    
    public boolean isUpToDown() {
        return !leftToRight;
    }

    public boolean isDownToUp() {
        return leftToRight;
    }

    public ProgressBar setUpToDown(final boolean upToDown) {
        this.leftToRight = upToDown;
        return setVertical(true);
    }

    public ProgressBar setDownToUp(final boolean downToUp) {
        return setUpToDown(!downToUp);
    }

    // Background & Foreground settings.

    public TextureProperties getBackgroundTexture() {
        return backgroundTexture;
    }

    public ProgressBar setBackgroundTexture(final TextureProperties backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    public List<TextureProperties> getProgressTextures() {
        return progressTextures;
    }

    public ProgressBar addForegroundTexture(final TextureProperties foregroundTex) {
        progressTextures.add(foregroundTex);
        return this;
    }

    // Tooltips

    @Override
    public List<String> getHoverTooltips(final WidgetGui widgetGui, final MousePos mousePos) {
        return tooltipFunction != null ? tooltipFunction.apply(this) : Collections.emptyList();
    }

    public ProgressBar setTooltipFunction(final Function<ProgressBar, List<String>> tooltipFunction) {
        this.tooltipFunction = tooltipFunction;
        return this;
    }

}
