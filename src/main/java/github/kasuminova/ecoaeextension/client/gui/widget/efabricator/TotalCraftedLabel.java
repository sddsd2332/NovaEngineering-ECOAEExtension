package github.kasuminova.ecoaeextension.client.gui.widget.efabricator;

import github.kasuminova.mmce.client.gui.widget.MultiLineLabel;
import github.kasuminova.mmce.client.gui.widget.container.Row;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.ecoaeextension.client.gui.widget.efabricator.event.EFGUIDataUpdateEvent;
import github.kasuminova.ecoaeextension.common.crafttweaker.util.NovaEngUtils;
import github.kasuminova.ecoaeextension.common.handler.EFabricatorEventHandler;
import net.minecraft.client.resources.I18n;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;

public class TotalCraftedLabel extends Row {

    private final MultiLineLabel label;
    private final Deque<Double> history = new ArrayDeque<>();
    private long value = 0;

    public TotalCraftedLabel() {
        this.label = new MultiLineLabel(Collections.singletonList(
                I18n.format("gui.efabricator.total_crafted", 0)
        ));
        this.label.setScale(.8f);
        this.label.setAutoWrap(false);
        this.addWidget(this.label);
    }

    @Override
    public boolean onGuiEvent(final GuiEvent event) {
        if (event instanceof EFGUIDataUpdateEvent efGUIEvent) {
            onValueUpdate(efGUIEvent.getEFGui().getData().totalCrafted());
        }
        return super.onGuiEvent(event);
    }

    public void onValueUpdate(final long newValue) {
        if (history.size() >= 10) {
            history.pollLast();
        }

        int added = (int) (newValue - value);
        if (value != 0 && added > 0) {
            double craftPerTick = ((added * (20F / EFabricatorEventHandler.UPDATE_INTERVAL)) / 20F);
            history.push(craftPerTick);
            double avgCraftPerTick = history.stream().mapToDouble(Double::doubleValue).average().orElse(0D);
            updateLabel(newValue, avgCraftPerTick);
        } else {
            history.push(0D);
            double avgCraftPerTick = history.stream().mapToDouble(Double::doubleValue).average().orElse(0D);
            if (avgCraftPerTick > 0) {
                updateLabel(newValue, avgCraftPerTick);
            } else {
                this.label.setContents(Collections.singletonList(
                        I18n.format("gui.efabricator.total_crafted", NovaEngUtils.formatDecimal(newValue))
                ));
            }
        }
        value = newValue;
    }

    private void updateLabel(final long newValue, final double avgCraftPerTick) {
        this.label.setContents(Collections.singletonList(
                I18n.format("gui.efabricator.total_crafted.update",
                        NovaEngUtils.formatDecimal(newValue),
                        avgCraftPerTick <= 0.1 ? "<0.1" : NovaEngUtils.formatDouble(avgCraftPerTick, 1)
                )
        ));
    }

}
