package github.kasuminova.ecoaeextension.client.gui.widget.efabricator.event;

import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;

public class EFPatternSearchContentUpdateEvent extends GuiEvent {

    private final String inputContent;
    private final String outputContent;

    public EFPatternSearchContentUpdateEvent(final String inputContent, final String outputContent) {
        super(null);
        this.inputContent = inputContent.toLowerCase();
        this.outputContent = outputContent.toLowerCase();
    }

    public String getInputContent() {
        return inputContent;
    }

    public String getOutputContent() {
        return outputContent;
    }

}

