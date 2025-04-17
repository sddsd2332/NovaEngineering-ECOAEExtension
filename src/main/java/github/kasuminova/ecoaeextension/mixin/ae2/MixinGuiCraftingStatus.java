package github.kasuminova.ecoaeextension.mixin.ae2;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.container.implementations.ContainerCraftingStatus;
import appeng.container.implementations.CraftingCPUStatus;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.Levels;
import github.kasuminova.ecoaeextension.common.ecalculator.ECPUStatus;
import github.kasuminova.ecoaeextension.common.integration.ae2.core.GuiColors;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@SuppressWarnings({"StaticVariableMayNotBeInitialized", "NonConstantFieldWithUpperCaseName"})
@Mixin(value = GuiCraftingStatus.class, remap = false)
public abstract class MixinGuiCraftingStatus extends AEBaseGui {

    @Shadow
    @Final
    private static int CPU_TABLE_WIDTH;

    @Shadow
    @Final
    private static int CPU_TABLE_SLOT_HEIGHT;

    @Shadow
    @Final
    private ContainerCraftingStatus status;

    @Shadow
    @Final
    private static int CPU_TABLE_SLOT_XOFF;

    @Shadow
    @Final
    private static int CPU_TABLE_SLOT_YOFF;

    @Shadow
    @Final
    private static int CPU_TABLE_SLOT_WIDTH;

    @Shadow
    private GuiScrollbar cpuScrollbar;

    @Unique
    private static final ResourceLocation novaeng_ec$TEXTURE = new ResourceLocation(ECOAEExtension.MOD_ID, "textures/gui/ecalculator_gui_2.png");

    @Unique
    private static final TextureProperties novaeng_ec$L4 = TextureProperties.of(novaeng_ec$TEXTURE, 1, 1, 67, 22);

    @Unique
    private static final TextureProperties novaeng_ec$L4_CELL = TextureProperties.of(novaeng_ec$TEXTURE, 0, 124, 16, 16);

    @Unique
    private static final TextureProperties novaeng_ec$L6 = TextureProperties.of(novaeng_ec$TEXTURE, 1, 26, 67, 22);

    @Unique
    private static final TextureProperties novaeng_ec$L6_CELL = TextureProperties.of(novaeng_ec$TEXTURE, 34, 124, 16, 16);

    @Unique
    private static final TextureProperties novaeng_ec$L9 = TextureProperties.of(novaeng_ec$TEXTURE, 1, 51, 67, 22);

    @Unique
    private static final TextureProperties novaeng_ec$L9_CELL = TextureProperties.of(novaeng_ec$TEXTURE, 17, 124, 16, 16);

    @Unique
    private static final TextureProperties novaeng_ec$L11 = TextureProperties.of(novaeng_ec$TEXTURE, 1, 76, 67, 22);

    public MixinGuiCraftingStatus() {
        super(null);
    }

    @WrapOperation(method = "drawFG", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"))
    private Object redirectDrawFG(final List<CraftingCPUStatus> instance, final int i, final Operation<CraftingCPUStatus> original, @Local(name = "hoveredCpu") final CraftingCPUStatus hoveredCpu) {
        CraftingCPUStatus status = original.call(instance, i);
        ECPUStatus ecpuStatus = (ECPUStatus) status;
        if (ecpuStatus.novaeng_ec$getLevel() != null) {
            novaeng_ec$renderECPUStatus(status, ecpuStatus.novaeng_ec$getLevel(), i, this.cpuScrollbar.getCurrentScroll(), hoveredCpu);
            return null;
        }
        return status;
    }

    @Unique
    @SuppressWarnings({"RedundantCast"})
    private void novaeng_ec$renderECPUStatus(final CraftingCPUStatus cpu, final Levels ecLevel, final int i, final int firstCpu, final CraftingCPUStatus hoveredCpu) {
        final FontRenderer font = fontRenderer;
        final int textColor = 0x202020;
        int x = -CPU_TABLE_WIDTH + 9;
        int y = 19 + (i - firstCpu) * CPU_TABLE_SLOT_HEIGHT;
        if (cpu.getSerial() == this.status.selectedCpuSerial) {
            GL11.glColor4f(0.0F, 0.8352F, 1.0F, 1.0F);
        } else if (hoveredCpu != null && hoveredCpu.getSerial() == cpu.getSerial()) {
            GL11.glColor4f(0.65F, 0.9F, 1.0F, 1.0F);
        } else {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
        this.bindTexture("guis/cpu_selector.png");
        this.drawTexturedModalRect(x, y, CPU_TABLE_SLOT_XOFF, CPU_TABLE_SLOT_YOFF, CPU_TABLE_SLOT_WIDTH, CPU_TABLE_SLOT_HEIGHT);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        TextFormatting color = TextFormatting.WHITE;
        if (ecLevel == Levels.L4) {
            novaeng_ec$L4.render(new RenderPos(x, y), (AEBaseGui) (Object) this);
            color = TextFormatting.AQUA;
        } else if (ecLevel == Levels.L6) {
            novaeng_ec$L6.render(new RenderPos(x, y), (AEBaseGui) (Object) this);
            color = TextFormatting.GOLD;
        } else if (ecLevel == Levels.L9) {
            novaeng_ec$L9.render(new RenderPos(x, y), (AEBaseGui) (Object) this);
            color = TextFormatting.DARK_PURPLE;
        } else if (ecLevel == Levels.L11) {
            novaeng_ec$L11.render(new RenderPos(x, y), (AEBaseGui) (Object) this);
            color = TextFormatting.RED;
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        String name = cpu.getName();
        if (name == null || name.isEmpty()) {
            name = color + "#" + cpu.getSerial() + TextFormatting.RESET;
        }
        if (name.replaceAll("§.", "").length() > 8) {
            name = name.substring(0, 7) + "..";
        }
        GL11.glPushMatrix();
        GL11.glTranslatef(x + (3 + 8), y + 3, 0);
        GL11.glScalef(0.8f, 0.8f, 1.0f);
        font.drawString(name, 0, 0, textColor);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslatef(x + 3, y + 11, 0);
        final IAEItemStack craftingStack = cpu.getCrafting();
        if (craftingStack != null) {
            GL11.glScalef(0.5f, 0.5f, 1.0f);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            if (ecLevel == Levels.L4) {
                novaeng_ec$L4_CELL.render(new RenderPos(2, 0), (AEBaseGui) (Object) this);
            } else if (ecLevel == Levels.L6) {
                novaeng_ec$L6_CELL.render(new RenderPos(2, 0), (AEBaseGui) (Object) this);
            } else if (ecLevel == Levels.L9) {
                novaeng_ec$L9_CELL.render(new RenderPos(2, 0), (AEBaseGui) (Object) this);
            }
            GL11.glTranslatef(18.0f, 3.5f, 0.0f);
            String amount = Long.toString(craftingStack.getStackSize());
            if (amount.length() > 9) {
                amount = amount.substring(0, 9) + "..";
            }
            GL11.glScalef(1.5f, 1.5f, 1.0f);
            font.drawString(amount, 0, 0, 0x009000);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            double craftingPercentage = (double) (cpu.getTotalItems() - Math.max(cpu.getRemainingItems(), 0)) / (double) cpu.getTotalItems();
            drawRect(x, y + CPU_TABLE_SLOT_HEIGHT - 3, x + (int) ((CPU_TABLE_SLOT_WIDTH - 1) * craftingPercentage), y + CPU_TABLE_SLOT_HEIGHT - 2, this.calculateGradientColor(craftingPercentage));
            GL11.glTranslatef(x + CPU_TABLE_SLOT_WIDTH - (19 + 4), y + 3, 0);
            this.drawItem(0, 0, craftingStack.createItemStack());
        } else {
            GL11.glScalef(0.5f, 0.5f, 1.0f);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            if (ecLevel == Levels.L4) {
                novaeng_ec$L4_CELL.render(new RenderPos(2, 0), (AEBaseGui) (Object) this);
            } else if (ecLevel == Levels.L6) {
                novaeng_ec$L6_CELL.render(new RenderPos(2, 0), (AEBaseGui) (Object) this);
            } else if (ecLevel == Levels.L9) {
                novaeng_ec$L9_CELL.render(new RenderPos(2, 0), (AEBaseGui) (Object) this);
            }
            GL11.glTranslatef(18.0f, 3.5f, 0.0f);
            GL11.glScalef(1.5f, 1.5f, 1.0f);
            font.drawString(cpu.formatStorage(), 0, 0, textColor);
        }
        GL11.glPopMatrix();
    }


    private static int processBarStartColorInt = GuiColors.ProcessBarStartColor.getColor();
    private static final int[] PROCESS_BAR_START_COLOR_INT_ARR = new int[]{(processBarStartColorInt >> 24) & 0xFF,
            (processBarStartColorInt >> 16) & 0xFF, (processBarStartColorInt >> 8) & 0xFF,
            processBarStartColorInt & 0xFF};

    private static int processBarMiddleColorInt = GuiColors.ProcessBarMiddleColor.getColor();
    private static final int[] PROCESS_BAR_MIDDLE_COLOR_INT_ARR = new int[]{(processBarMiddleColorInt >> 24) & 0xFF,
            (processBarMiddleColorInt >> 16) & 0xFF, (processBarMiddleColorInt >> 8) & 0xFF,
            processBarMiddleColorInt & 0xFF};

    private static int processBarEndColorInt = GuiColors.ProcessBarEndColor.getColor();
    private static final int[] PROCESS_BAR_END_COLOR_INT_ARR = new int[]{(processBarEndColorInt >> 24) & 0xFF,
            (processBarEndColorInt >> 16) & 0xFF, (processBarEndColorInt >> 8) & 0xFF, processBarEndColorInt & 0xFF};

    private int calculateGradientColor(double percentage) {
        int start[] = null;
        int end[] = null;
        double ratio = 0;
        if (percentage <= 0.5) {
            start = PROCESS_BAR_START_COLOR_INT_ARR;
            end = PROCESS_BAR_MIDDLE_COLOR_INT_ARR;
            ratio = percentage * 2;
        } else {
            start = PROCESS_BAR_MIDDLE_COLOR_INT_ARR;
            end = PROCESS_BAR_END_COLOR_INT_ARR;
            ratio = (percentage - 0.5d) * 2;
        }
        int a = (int) (start[0] + ratio * (end[0] - start[0]));
        int r = (int) (start[1] + ratio * (end[1] - start[1]));
        int g = (int) (start[2] + ratio * (end[2] - start[2]));
        int b = (int) (start[3] + ratio * (end[3] - start[3]));
        return (a << 24) | (r << 16) | (g << 8) | (b);
    }

}
