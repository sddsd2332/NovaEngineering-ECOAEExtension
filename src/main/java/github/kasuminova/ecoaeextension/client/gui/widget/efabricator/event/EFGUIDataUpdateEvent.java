package github.kasuminova.ecoaeextension.client.gui.widget.efabricator.event;

import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.ecoaeextension.client.gui.GuiEFabricatorController;

public class EFGUIDataUpdateEvent extends GuiEvent {

    private final GuiEFabricatorController gui;
    
    public EFGUIDataUpdateEvent(final GuiEFabricatorController gui) {
        super(null);
        this.gui = gui;
    }

    public GuiEFabricatorController getEFGui() {
        return gui;
    }

}
