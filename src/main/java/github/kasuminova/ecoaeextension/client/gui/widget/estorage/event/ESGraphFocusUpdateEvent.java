package github.kasuminova.ecoaeextension.client.gui.widget.estorage.event;

import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.ecoaeextension.client.gui.widget.estorage.Graph;

public class ESGraphFocusUpdateEvent extends GuiEvent {

    protected final Graph graph;

    public ESGraphFocusUpdateEvent(Graph graph) {
        super(null);
        this.graph = graph;
    }

    public Graph getGraph() {
        return graph;
    }
}

