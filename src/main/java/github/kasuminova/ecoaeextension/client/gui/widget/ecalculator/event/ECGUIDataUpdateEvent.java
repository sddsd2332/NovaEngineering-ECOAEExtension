package github.kasuminova.ecoaeextension.client.gui.widget.ecalculator.event;

import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.ecoaeextension.client.gui.GuiECalculatorController;

public class ECGUIDataUpdateEvent extends GuiEvent {

    private final GuiECalculatorController gui;

    public ECGUIDataUpdateEvent(final GuiECalculatorController gui) {
        super(null);
        this.gui = gui;
    }

    public GuiECalculatorController getECGui() {
        return gui;
    }

}
