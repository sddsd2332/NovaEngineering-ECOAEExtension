package github.kasuminova.ecoaeextension.client.gui.widget.efabricator.event;

import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.ecoaeextension.client.gui.GuiEFabricatorPatternSearch;

public class EFPatternSearchGUIUpdateEvent extends GuiEvent {

    private final GuiEFabricatorPatternSearch gui;
    private final boolean fullUpdate;

    public EFPatternSearchGUIUpdateEvent(final GuiEFabricatorPatternSearch gui, final boolean fullUpdate) {
        super(null);
        this.gui = gui;
        this.fullUpdate = fullUpdate;
    }

    public GuiEFabricatorPatternSearch getEFGui() {
        return gui;
    }

    public boolean isFullUpdate() {
        return fullUpdate;
    }

}
