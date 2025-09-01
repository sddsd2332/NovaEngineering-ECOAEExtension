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
import net.minecraftforge.fml.common.Loader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiPatternTerm.class)
public class MixinGuiPatternTerm extends GuiMEMonitorable {

    @Final
    @Shadow(remap = false)
    private ContainerPatternEncoder container;

    @Unique
    private GuiTabButton ecoaeextension$uploadPatternButton;

    @SuppressWarnings("DataFlowIssue")
    public MixinGuiPatternTerm() {
        super(null, null);
    }

    @Inject(method = "initGui", at = @At("RETURN"))
    private void injectInitGui(final CallbackInfo ci) {
        int baseX = this.guiLeft + 173;
        int baseY = this.guiTop + this.ySize - 155;
        ecoaeextension$uploadPatternButton = new GuiTabButton(baseX, baseY, new ItemStack(BlockEFabricatorController.L4), I18n.format("gui.efabricator.button.upload_pattern"), this.itemRender);
        this.buttonList.add(this.ecoaeextension$uploadPatternButton);
        if (Loader.isModLoaded("crazyae")) {
            int targetY = baseY;
            for (final GuiButton b : this.buttonList) {
                if (b == this.ecoaeextension$uploadPatternButton) {
                    continue;
                }
                if (b instanceof GuiTabButton && b.x == baseX) {
                    int candidateY = b.y + b.height;
                    if (candidateY > targetY) {
                        targetY = candidateY;
                    }
                }
            }
            this.ecoaeextension$uploadPatternButton.y = targetY;
        }
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    private void injectActionPerformed(final GuiButton btn, final CallbackInfo ci) {
        if (btn == ecoaeextension$uploadPatternButton) {
            ECOAEExtension.NET_CHANNEL.sendToServer(new PktPatternTermUploadPattern());
            ci.cancel();
        }
    }

    @Inject(method = "drawFG", at = @At("HEAD"), remap = false)
    private void injectDrawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY, final CallbackInfo ci) {
        ecoaeextension$uploadPatternButton.visible = this.container.isCraftingMode();
    }

}
