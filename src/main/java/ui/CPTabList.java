package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

public class CPTabList extends HBox {

    private CPTabHeader header = new CPTabHeader();
    private StackPane content = new StackPane();

    private ObservableList<CPTabWrapper> tabs = FXCollections.observableArrayList();

    public CPTabList() {
        super();

        HBox.setHgrow(header, Priority.NEVER);
        HBox.setHgrow(content, Priority.ALWAYS);

        this.header.setItems(this.tabs);
        this.getChildren().addAll(header, content);
    }

    public void addTab(CPTab tab, Node content) {
        this.tabs.add(new CPTabWrapper(tab, content, () -> {
            for (CPTabWrapper tabWrapper: this.tabs) {
                this.setTabAsActive(tab);
            }
        }));

        this.content.getChildren().add(content);
    }

    public void removeTab(String id) {
        for (CPTabWrapper tabWrapper: this.tabs) {
            if (tabWrapper.tab.id.equals(id)) {
                this.content.getChildren().remove(tabWrapper.content);
                this.tabs.remove(tabWrapper);
                break;
            }
        }

        for (CPTabWrapper tabWrapper: this.tabs) {
            tabWrapper.content.setVisible(true);
            break;
        }
    }

    // Helpers
    private void setTabAsActive(CPTab tab) {
        for (CPTabWrapper tabWrapper: this.tabs) {
            if (tabWrapper.tab.id.equals(tab.id)) {
                tabWrapper.content.setVisible(true);
            } else {
                tabWrapper.content.setVisible(false);
            }
        }
    }
}
