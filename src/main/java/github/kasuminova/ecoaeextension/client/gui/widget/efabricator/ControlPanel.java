package github.kasuminova.ecoaeextension.client.gui.widget.efabricator;

import github.kasuminova.mmce.client.gui.widget.container.Row;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.client.gui.GuiEFabricatorController;
import github.kasuminova.ecoaeextension.client.gui.widget.Button6State;
import github.kasuminova.ecoaeextension.client.gui.widget.efabricator.event.EFGUIDataUpdateEvent;
import github.kasuminova.ecoaeextension.common.network.PktEFabricatorGUIAction;
import github.kasuminova.ecoaeextension.common.util.MachineCoolants;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class ControlPanel extends Row {
    private final Button6State overclocking = new Button6State();
    private final Button6State activeCooling = new Button6State();

    public ControlPanel() {
        addWidgets(
                overclocking
                        .setClickedMouseDownTexture(75, 1)
                        .setClickedTexture(1, 1)
                        .setMouseDownTexture(75, 38)
                        .setTexture(1, 38)
                        .setHoveredTexture(1, 38)
                        .setTextureLocation(GuiEFabricatorController.TEXTURES_ELEMENTS)
                        .setOnClickedListener(button -> {
                            if (overclocking.isClicked()) {
                                ECOAEExtension.NET_CHANNEL.sendToServer(new PktEFabricatorGUIAction(PktEFabricatorGUIAction.Action.ENABLE_OVERCLOCKING));
                            } else {
                                ECOAEExtension.NET_CHANNEL.sendToServer(new PktEFabricatorGUIAction(PktEFabricatorGUIAction.Action.DISABLE_OVERCLOCKING));
                            }
                        })
                        .setTooltipFunction(button -> {
                            List<String> tooltips = new ArrayList<>();
                            tooltips.add(overclocking.isClicked()
                                    ? I18n.format("gui.efabricator.overclocked.disable.tip")
                                    : I18n.format("gui.efabricator.overclocked.enable.tip")
                            );
                            return tooltips;
                        })
                        .setWidthHeight(36, 36)
                        .setMarginRight(4),
                activeCooling
                        .setClickedMouseDownTexture(112, 1)
                        .setClickedTexture(38, 1)
                        .setMouseDownTexture(112, 38)
                        .setTexture(38, 38)
                        .setHoveredTexture(38, 38)
                        .setTextureLocation(GuiEFabricatorController.TEXTURES_ELEMENTS)
                        .setOnClickedListener(button -> {
                            if (activeCooling.isClicked()) {
                                ECOAEExtension.NET_CHANNEL.sendToServer(new PktEFabricatorGUIAction(PktEFabricatorGUIAction.Action.ENABLE_ACTIVE_COOLANT));
                            } else {
                                ECOAEExtension.NET_CHANNEL.sendToServer(new PktEFabricatorGUIAction(PktEFabricatorGUIAction.Action.DISABLE_ACTIVE_COOLANT));
                            }
                        })
                        .setTooltipFunction(button -> {
                            List<String> tooltips = new ArrayList<>();
                            if (activeCooling.isClicked()) {
                                tooltips.add(I18n.format("gui.efabricator.active_cooling.disable.tip"));
                            } else {
                                tooltips.add(I18n.format("gui.efabricator.active_cooling.enable.tip.0"));
                                tooltips.add(I18n.format("gui.efabricator.active_cooling.enable.tip.1"));
                                MachineCoolants.INSTANCE.getCoolants().forEach(coolant -> {
                                    FluidStack input = coolant.input();
                                    String name = input.getFluid().getLocalizedName(input);
                                    FluidStack output = coolant.output();
                                    if (output == null) {
                                        tooltips.add(I18n.format("gui.efabricator.active_cooling.enable.tip.1.coolant.no_output",
                                                name, input.amount, coolant.coolantUnit()
                                        ));
                                    } else {
                                        String outputName = output.getFluid().getLocalizedName(output);
                                        tooltips.add(I18n.format("gui.efabricator.active_cooling.enable.tip.1.coolant",
                                                name, input.amount, outputName, output.amount, coolant.coolantUnit()
                                        ));
                                    }
                                });
                                tooltips.add(I18n.format("gui.efabricator.active_cooling.enable.tip.2"));
                                tooltips.add(I18n.format("gui.efabricator.active_cooling.enable.tip.3"));
                                tooltips.add(I18n.format("gui.efabricator.active_cooling.enable.tip.4"));
                                tooltips.add(I18n.format("gui.efabricator.active_cooling.enable.tip.5"));
                                tooltips.add(I18n.format("gui.efabricator.active_cooling.enable.tip.6"));
                            }
                            return tooltips;
                        })
                        .setWidthHeight(36, 36)
                        .setMarginRight(4),
                new StatisticPanel()
        );
    }

    @Override
    public boolean onGuiEvent(final GuiEvent event) {
        if (event instanceof EFGUIDataUpdateEvent efGuiEvent) {
            GuiEFabricatorController efGui = efGuiEvent.getEFGui();
            overclocking.setClicked(efGui.getData().overclocked());
            activeCooling.setClicked(efGui.getData().activeCooling());
        }
        return super.onGuiEvent(event);
    }

}
