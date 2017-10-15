package client.core.section.session;

import client.core.section.SectionBaseController;
import client.core.section.UISection;
import client.model.Message;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import ui.CPTextField;
import util.ProtocolHandler;
import util.ValidationHandler;

public class SessionController extends SectionBaseController implements UISection {
    private static final String TEMPLATE_PATH = "templates/session.fxml";

    private SessionWorker sessionWorker;
    private Runnable removeSession;

    @FXML
    private ListView<String> userListView;
    private ObservableList<String> userObservableList = FXCollections.observableArrayList();;

    @FXML
    private Label usernameLabel;

    @FXML
    private ListView<Message> messageListView;
    private ObservableList<Message> messageObservableList = FXCollections.observableArrayList();;

    @FXML
    private CPTextField messageTextField;
    private BooleanProperty isMessageValid = new SimpleBooleanProperty(false);

    @FXML
    private GridPane disconnectGridPane;

    public SessionController(SessionWorker sessionWorker, Runnable removeSession) {
        this.sessionWorker = sessionWorker;
        this.removeSession = removeSession;

        this.sessionWorker.addLists(messageObservableList, userObservableList);
    }

    @FXML
    public void initialize() {
        super.initialize();
        this.sessionWorker.start();

        super.isDisabled.bind(
            isMessageValid.not()
        );

        this.sessionWorker.setOnDisconnect(() -> disconnectGridPane.setVisible(true));
        disconnectGridPane.managedProperty().bind(disconnectGridPane.visibleProperty());
        disconnectGridPane.setVisible(false);

        messageListView.setCellFactory(param -> new MessageCell());
        usernameLabel.setText(sessionWorker.username);

        messageListView.setItems(messageObservableList);
        userListView.setItems(userObservableList);

        messageTextField.setMaxLength(ProtocolHandler.Validation.Rule.Message.MAX_LENGTH);

        // Validation
        messageTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isMessageValid.set(ValidationHandler.validateMessage(newValue).success);
        });

        // Call handleOKAction on ENTER
        messageTextField.setOnKeyPressed((keyEvent) -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                super.submitButton.fire();
            }
        });
    }

    public String getTemplatePath() {
        return TEMPLATE_PATH;
    }

    @FXML
    public void handleOKAction(ActionEvent event) {
        String message = messageTextField.getText().trim();
        if (!message.isEmpty()) {
            this.sessionWorker.sendMessage(message);
            messageTextField.setText(null);
        }
    }

    @FXML
    public void handleDISCONNECTAction(ActionEvent event) {
        this.sessionWorker.disconnect();
        this.removeSession.run();
    }

    @FXML
    public void handleCLOSEAction(ActionEvent event) {
        this.removeSession.run();
    }

    // Custom cell for the message list
    private class MessageCell extends ListCell<Message> {
        private final Label authorLabel = new Label();
        private final Label messageLabel = new Label();

        private MessageCell() {
            super();

            this.authorLabel.getStyleClass().add("message-author");
            this.messageLabel.getStyleClass().add("message-content");

            this.authorLabel.setPrefWidth(130);
            this.authorLabel.minWidthProperty().bind(this.authorLabel.prefWidthProperty());
            this.authorLabel.setAlignment(Pos.CENTER_RIGHT);
            this.messageLabel.setWrapText(true);

            HBox.setHgrow(this.authorLabel, Priority.NEVER);
            HBox.setHgrow(this.messageLabel, Priority.NEVER);
        }

        @Override
        public void updateItem(Message item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                this.authorLabel.setText(item.author.getValue().trim() + ":");
                this.messageLabel.setText(item.message.getValue().trim());

                HBox wrapper = new HBox(this.authorLabel, this.messageLabel);
                if (item.author.getValue().equals(sessionWorker.username)) {
                    wrapper.getStyleClass().add("current-author");
                }

                wrapper.prefWidthProperty().bind(this.getListView().widthProperty().subtract(12));

                setText(null);
                setGraphic(wrapper);
            }
        }
    }
}
