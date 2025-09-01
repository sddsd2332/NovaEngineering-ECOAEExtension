package github.kasuminova.ecoaeextension.client.gui.widget.estorage;

import github.kasuminova.mmce.client.gui.util.AnimationValue;
import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.client.gui.GuiEStorageController;
import github.kasuminova.ecoaeextension.common.util.ColorUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class EStorageGraphBar extends DynamicWidget {
    public static final ResourceLocation TEX_RES = new ResourceLocation(ECOAEExtension.MOD_ID, "textures/gui/estorage_controller_elements.png");

    public static final int BAR_TEX_X = 223;
    public static final int BAR_TEX_Y = 163;

    public static final int BAR_WIDTH = 32;
    public static final int BAR_HEIGHT = 92;

    public static final int TOP_TEX_X = 1;
    public static final int TOP_TEX_Y = 246;

    public static final int MID_TEX_X = 34;
    public static final int MID_TEX_Y = 250;
    public static final int MID_TEX_HEIGHT = 4;

    public static final int BOTTOM_TEX_X = 1;
    public static final int BOTTOM_TEX_Y = 246;

    public static final int BOTTOM_AND_TOP_HEIGHT = 8;

    protected final GuiEStorageController controllerGUI;

    protected AnimationValue percentage = AnimationValue.ofFinished(0, 500, .25, .1, .25, 1);
    protected AnimationValue ref = null;
    protected boolean reverseColor = false;

    public EStorageGraphBar(final GuiEStorageController controllerGUI) {
        this.controllerGUI = controllerGUI;
        setWidthHeight(BAR_WIDTH, BAR_HEIGHT);
    }

    public void setPercentage(final AnimationValue ref, final boolean reverseColor) {
        if (this.ref != ref) {
            this.percentage.set(ref.getTargetValue());
            this.ref = ref;
            this.reverseColor = reverseColor;
        }
    }

    @Override
    public void update(final WidgetGui gui) {
        super.update(gui);
        if (ref != null && ref.getTargetValue() != percentage.getTargetValue()) {
            percentage.set(ref.getTargetValue());
        }
    }

    @Override
    public void preRender(final WidgetGui widgetGui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        if (percentage.get() <= 0) {
            return;
        }

        GuiScreen gui = widgetGui.getGui();
        gui.mc.getTextureManager().bindTexture(TEX_RES);

        // 计算柱形图的高度
        int barHeight = (int) Math.round(((BAR_HEIGHT - (BOTTOM_AND_TOP_HEIGHT)) * percentage.get()));

        // 计算渐变色
        Color startColor = new Color(0, 255, 0);
        Color midColor = new Color(255, 255, 0);
        Color endColor = new Color(255, 0, 0);
        Color barColor = ColorUtils.getGradientColor(reverseColor ? new Color[]{endColor, midColor, startColor, startColor} : new Color[]{startColor, startColor, midColor, endColor},
                (int) (255 * .75F),
                (float) percentage.get()
        );

        GlStateManager.color(
                (float) barColor.getRed() / 255,
                (float) barColor.getGreen() / 255,
                (float) barColor.getBlue() / 255,
                .75F
        );

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        // 渲染顶部
        gui.drawTexturedModalRect(
                renderPos.posX(), renderPos.posY() + ((BAR_HEIGHT - (barHeight + BOTTOM_AND_TOP_HEIGHT))),
                TOP_TEX_X, TOP_TEX_Y,
                BAR_WIDTH, BOTTOM_AND_TOP_HEIGHT
        );

        // 渲染中间部分
        int yOffset = renderPos.posY() + (BAR_HEIGHT - (barHeight + BOTTOM_AND_TOP_HEIGHT / 2)) + 1;
        int finalYOffset = renderPos.posY() + BAR_HEIGHT - BOTTOM_AND_TOP_HEIGHT + (BOTTOM_AND_TOP_HEIGHT / 2) + 1;
        for (int i = yOffset; i < finalYOffset; i++) {
            gui.drawTexturedModalRect(
                    renderPos.posX(), i,
                    MID_TEX_X, MID_TEX_Y,
                    BAR_WIDTH, MID_TEX_HEIGHT
            );
        }

        // 渲染底部
        gui.drawTexturedModalRect(
                renderPos.posX(), renderPos.posY() + BAR_HEIGHT - BOTTOM_AND_TOP_HEIGHT,
                BOTTOM_TEX_X, BOTTOM_TEX_Y,
                BAR_WIDTH, BOTTOM_AND_TOP_HEIGHT
        );

        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    @Override
    public void render(final WidgetGui widgetGui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
    }

}
