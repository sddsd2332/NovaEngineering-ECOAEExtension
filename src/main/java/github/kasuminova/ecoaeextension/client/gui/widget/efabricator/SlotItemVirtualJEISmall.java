package github.kasuminova.ecoaeextension.client.gui.widget.efabricator;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.slot.SlotItemVirtualJEI;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class SlotItemVirtualJEISmall extends SlotItemVirtualJEI {

    public SlotItemVirtualJEISmall() {
        this(ItemStack.EMPTY);
    }

    public SlotItemVirtualJEISmall(final ItemStack stackInSlot) {
        super(stackInSlot);
        setWidthHeight(10, 10);
    }

    public static SlotItemVirtualJEISmall of() {
        return new SlotItemVirtualJEISmall();
    }

    public static SlotItemVirtualJEISmall of(final ItemStack stackInSlot) {
        return new SlotItemVirtualJEISmall(stackInSlot);
    }

    @Override
    public void render(final WidgetGui widgetGui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(.5, .5, 1);
        super.render(widgetGui, renderSize, renderPos.add(renderPos).add(new RenderPos(1, 1)), mousePos);
        GlStateManager.popMatrix();
    }

    @Override
    protected void drawHoverOverlay(final MousePos mousePos, final int rx, final int ry) {
        if (mouseOver) {
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);
            GuiScreen.drawRect(rx, ry, rx + 16, ry + 16, new Color(255, 255, 255, 150).getRGB());
            GlStateManager.enableBlend();
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }

}
