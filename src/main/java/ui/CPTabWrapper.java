package ui;

import javafx.scene.Node;

public class CPTabWrapper {
    protected CPTab tab;
    protected Node content;
    protected Runnable onActionRequest;

    public CPTabWrapper(CPTab tab, Node content, Runnable onActionRequest) {
        this.tab = tab;
        this.content = content;
        this.onActionRequest = onActionRequest;
    }
}
