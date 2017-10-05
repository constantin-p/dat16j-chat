package ui;

import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class CPTabHeader extends ListView<CPTabWrapper> {
    private static final String COMMAND_PREFIX = (System.getProperty("os.name").indexOf("Mac") >= 0)
            ? "⌘"
            : "Ctrl";
    private static PseudoClass NOTIFY_PSEUDO_CLASS = PseudoClass.getPseudoClass("notification");

    public CPTabHeader() {
        super();

        this.setPrefWidth(CPTabHeaderCell.SIZE * 1.6);
        this.setMinWidth(this.getPrefWidth());
        this.setCellFactory(param -> new CPTabHeaderCell());
    }

    private class CPTabHeaderCell extends ListCell<CPTabWrapper> {
        protected static final int SIZE = 26;

        private final VBox wrapper = new VBox();
        private final StackPane pane = new StackPane();
        private final Label label = new Label();
        private final Label shortcut = new Label();

        private CPTabHeaderCell() {
            super();
            this.setupGraphic();

            // Select tab
            setOnMouseClicked(event -> {
                CPTabWrapper item = getItem();
                if (item == null) {
                    return;
                }
                item.onActionRequest.run();
            });

            // Tab reordering
            setOnDragDetected(event -> {
                CPTabWrapper item = getItem();
                if (item == null) {
                    return;
                }

                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(item.tab.id);
                dragboard.setDragView(this.pane.snapshot(null, null));
                dragboard.setContent(content);

                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }

                event.consume();
            });

            setOnDragEntered(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                    setOpacity(0.3);
                }
            });

            setOnDragExited(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                    setOpacity(1);
                }
            });

            setOnDragDropped(event -> {
                CPTabWrapper item = getItem();
                if (item == null) {
                    return;
                }

                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString()) {
                    ObservableList<CPTabWrapper> items = getListView().getItems();
                    CPTabWrapper draggedItem = this.getNodeItem(items, db.getString());

                    int draggedIndex = items.indexOf(draggedItem);
                    int thisIndex = items.indexOf(item);

                    items.set(draggedIndex, items.get(thisIndex));
                    items.set(thisIndex, draggedItem);

                    List<CPTabWrapper> itemsCopy = new ArrayList<>(getListView().getItems());
                    getListView().getItems().setAll(itemsCopy);

                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            });

            setOnDragDone(DragEvent::consume);
        }

        @Override
        public void updateItem(CPTabWrapper item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                ObservableList<CPTabWrapper> items = getListView().getItems();

                this.label.setText(item.tab.getInitials());
                this.shortcut.setText(COMMAND_PREFIX + Integer.toString(items.indexOf(item)));

                item.tab.hasNotifications.addListener(e -> {
                    System.out.println(e);
                    this.pane.pseudoClassStateChanged(NOTIFY_PSEUDO_CLASS, item.tab.hasNotifications.get());
                });

                setText(null);
                setGraphic(this.wrapper);
            }
        }

        private void setupGraphic() {
            this.wrapper.setFillWidth(true);
            this.wrapper.setAlignment(Pos.CENTER);

            this.pane.setPrefHeight(SIZE);
            this.pane.setPrefWidth(SIZE);
            this.pane.setMaxWidth(SIZE);

            StackPane.setAlignment(this.label, Pos.CENTER);

            this.wrapper.getStyleClass().add("cp-tab");
            this.pane.getStyleClass().add("cp-tab-cell");
            this.label.getStyleClass().add("cp-tab-cell-label");
            this.shortcut.getStyleClass().add("cp-tab-shortcut");

            this.pane.getChildren().add(this.label);
            this.wrapper.getChildren().add(this.pane);
            this.wrapper.getChildren().add(this.shortcut);
        }

        private CPTabWrapper getNodeItem(List<CPTabWrapper> items, String id) {
            for (CPTabWrapper item : items) {
                if (item.tab.id.equals(id)) {
                    return item;
                }
            }
            return null;
        }
    }
}
