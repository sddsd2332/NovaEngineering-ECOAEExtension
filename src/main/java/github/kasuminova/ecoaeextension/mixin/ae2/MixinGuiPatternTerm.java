package github.kasuminova.ecoaeextension.mixin.ae2;

import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerPatternEncoder;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.ecotech.efabricator.BlockEFabricatorController;
import github.kasuminova.ecoaeextension.common.network.PktPatternTermUploadPattern;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(GuiPatternTerm.class)
public class MixinGuiPatternTerm extends GuiMEMonitorable {

    @Final
    @Shadow(remap = false)
    private ContainerPatternEncoder container;
    @Shadow(remap = false)
    private GuiTabButton tabCraftButton;
    @Shadow(remap = false)
    private GuiTabButton tabProcessButton;
    @Unique
    private GuiTabButton ecoaeextension$uploadPatternButton;

    @SuppressWarnings("DataFlowIssue")
    public MixinGuiPatternTerm() {
        super(null, null);
    }

    //确保我们是最后注入的
    @Inject(method = "initGui", at = @At("TAIL"), order = 9999)
    private void injectInitGui(CallbackInfo ci) {
        int baseX = guiLeft + 173;
        int baseY = guiTop + ySize - 177;
        int offsetY = baseY + 22;
        ecoaeextension$uploadPatternButton = new GuiTabButton(baseX, offsetY, new ItemStack(BlockEFabricatorController.L4), I18n.format("gui.efabricator.button.upload_pattern"), this.itemRender);
        this.buttonList.add(ecoaeextension$uploadPatternButton);
        //让其他的MOD添加的tab在我们的后面
        int targetY = offsetY;
        //获取其他mod的tab按钮
        List<GuiButton> buttons = buttonList.stream().filter(list -> list instanceof GuiTabButton button && button != tabProcessButton && button != tabCraftButton && button != ecoaeextension$uploadPatternButton).collect(Collectors.toList());
        if (!buttons.isEmpty()) {
            for (GuiButton b : buttons) {
                if (b instanceof GuiTabButton tab) {
                    if (tab.isVisible()) {
                        if (tab.x == baseX) {
                            int candidateY = tab.y + tab.height;
                            if (candidateY > targetY) {
                                targetY = candidateY;
                            }
                            //重新应用tab的Y位置
                            tab.y = targetY;
                            int baseOffsetY = baseY % tab.height;
                            tab.y = tab.y - (tab.y % tab.height) + baseOffsetY;
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    private void injectActionPerformed(GuiButton btn, CallbackInfo ci) {
        if (btn == ecoaeextension$uploadPatternButton) {
            ECOAEExtension.NET_CHANNEL.sendToServer(new PktPatternTermUploadPattern());
            ci.cancel();
        }
    }

    @Inject(method = "drawFG", at = @At("HEAD"), remap = false)
    private void injectDrawFG(int offsetX, int offsetY, int mouseX, int mouseY, CallbackInfo ci) {
        ecoaeextension$uploadPatternButton.visible = container.isCraftingMode();
    }

}
