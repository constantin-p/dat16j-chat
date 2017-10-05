package client.core.section.connect;

import client.core.RootController;
import client.core.section.SectionBaseController;
import client.core.section.UISection;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import util.Response;
import util.ValidationHandler;

import java.net.Socket;

public class ConnectController extends SectionBaseController implements UISection {
    private static final String TEMPLATE_PATH = "templates/section/connect.fxml";

    private RootController rootController;
    private Socket socket;

    @FXML
    private Label errorLabel;

    @FXML
    private Button joinButton;
    @FXML
    private Button cancelButton;
    @FXML
    private ProgressIndicator loadingProgressIndicator;
    @FXML
    private Label loadingLabel;

    @FXML
    private TextField usernameTextField;
    private BooleanProperty isUsernameValid = new SimpleBooleanProperty(false);

    @FXML
    private TextField hostTextField;
    private BooleanProperty isHostValid = new SimpleBooleanProperty(false);

    @FXML
    private TextField portTextField;
    private BooleanProperty isPortValid = new SimpleBooleanProperty(false);


    public ConnectController(RootController rootController) {
        this.rootController = rootController;
    }

    @FXML
    public void initialize() {
        super.initialize();

        super.isDisabled.bind(
                isUsernameValid.not().or(
                        isHostValid.not().or(
                                isPortValid.not()
                        )
                )
        );

        joinButton.disableProperty().bind(isUsernameValid.not());

        loadingProgressIndicator.managedProperty().bind(loadingProgressIndicator.visibleProperty());
        loadingProgressIndicator.setVisible(false);
        cancelButton.managedProperty().bind(cancelButton.visibleProperty());
        cancelButton.setVisible(false);
        joinButton.managedProperty().bind(joinButton.visibleProperty());
        joinButton.setVisible(false);

        // Validation
        usernameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isUsernameValid.set(ValidationHandler.validateControl(usernameTextField, errorLabel,
                    ValidationHandler.validateUsername(newValue)));
        });

        hostTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isHostValid.set(ValidationHandler.validateControl(hostTextField, errorLabel,
                    ValidationHandler.validateHost(newValue)));
        });

        portTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isPortValid.set(ValidationHandler.validateControl(portTextField, errorLabel,
                    ValidationHandler.validatePort(newValue)));
        });

    }

    public String getTemplatePath() {
        return TEMPLATE_PATH;
    }

    @FXML
    public void handleOKAction(ActionEvent event) {
        String username = this.usernameTextField.getText();
        String host = this.hostTextField.getText();
        int port = Integer.valueOf(this.portTextField.getText());

        // 1. Disable fields
        this.usernameTextField.setDisable(true);
        this.hostTextField.setDisable(true);
        this.portTextField.setDisable(true);

        // 2. Start connect service
        this.setupConnectTaskUI(host, port, username);
    }

    @FXML
    public void handleJOINAction(ActionEvent event) {
        if (this.socket != null) {
            String username = this.usernameTextField.getText();

            this.setupJoinTaskUI(this.socket, username);
        }
    }

    // Helpers
    private void setupConnectTaskUI(String host, int port, String username) {
        ConnectWorker connectWorker = new ConnectWorker(host, port, () -> {
            // Start
            // 1. Update UI
            super.submitButton.setVisible(false);
            this.loadingProgressIndicator.setVisible(true);
            this.cancelButton.setVisible(true);
            this.loadingLabel.setText("Connecting     ");
            System.out.println("Start");
        }, (Socket socket) -> {
            // Success
            // 1. Hide task related ui (Cancel, Label, ProgressIndicator)
            this.cancelButton.setVisible(false);
            this.loadingProgressIndicator.setVisible(false);

            // 2. Update UI
            this.loadingLabel.setText("Connected");

            // 3. Start JOIN task
            this.socket = socket;
            this.setupJoinTaskUI(this.socket, username);
        }, () -> {
            // Failure
            // 1. Hide task related ui (Cancel, Label, ProgressIndicator)
            this.loadingProgressIndicator.setVisible(false);
            this.cancelButton.setVisible(false);
            this.loadingLabel.setText(null);

            // 2. Show relevant UI
            this.usernameTextField.setDisable(false);
            this.hostTextField.setDisable(false);
            this.portTextField.setDisable(false);

            super.submitButton.setVisible(true);
            ValidationHandler.showError(this.errorLabel, new Response(false, ValidationHandler.Error.CONNECTION));
        }, () -> {
            // Cancellation
            // 1. Hide task related ui (Cancel, Label, ProgressIndicator)
            this.loadingProgressIndicator.setVisible(false);
            this.cancelButton.setVisible(false);
            this.loadingLabel.setText(null);

            // 2. Show relevant UI
            this.usernameTextField.setDisable(false);
            this.hostTextField.setDisable(false);
            this.portTextField.setDisable(false);

            super.submitButton.setVisible(true);
            ValidationHandler.showError(this.errorLabel, new Response(true));

            System.out.println("Cancellation");
        });
        this.cancelButton.setOnAction((actionEvent) -> {
            connectWorker.cancel();
        });
    }

    private void setupJoinTaskUI(Socket socket, String username) {
        JoinWorker joinWorker = new JoinWorker(socket, username, () -> {
            // Start
            // 1. Update UI
            this.joinButton.setVisible(false);
            this.loadingProgressIndicator.setVisible(true);
            this.cancelButton.setVisible(true);
            this.loadingLabel.setText("Joining     ");
            System.out.println("Start");
        }, (Response response) -> {
            // Success
            // 1. Hide task related ui (Cancel, Label, ProgressIndicator)
            this.cancelButton.setVisible(false);
            this.loadingProgressIndicator.setVisible(false);
            this.loadingLabel.setText(null);

            // 2. Update UI
            if (response.success) {
                // A. Add session
                this.addChat(response.msg);
            } else {
                // B. Choose another username or reset the connection
                this.usernameTextField.setDisable(false);
                this.cancelButton.setVisible(true);
                this.loadingLabel.setText("     ");
                this.cancelButton.setOnAction((actionEvent) -> {
                    this.resetConnectionScreen();
                });

                this.joinButton.setVisible(true);
                ValidationHandler.showError(this.errorLabel, response);
            }
        }, () -> {
            // Failure
            // 1. Hide task related ui (Cancel, Label, ProgressIndicator)
            this.loadingProgressIndicator.setVisible(false);
            this.cancelButton.setVisible(false);
            this.loadingLabel.setText(null);

            // 2. Show relevant UI
            this.usernameTextField.setDisable(false);
            this.hostTextField.setDisable(true);
            this.portTextField.setDisable(true);

            this.joinButton.setVisible(true);
            ValidationHandler.showError(this.errorLabel, new Response(false, ValidationHandler.Error.CONNECTION));
        }, () -> {
            // Cancellation
            // 1. Hide task related ui (Cancel, Label, ProgressIndicator)
            this.loadingProgressIndicator.setVisible(false);
            this.cancelButton.setVisible(false);
            this.loadingLabel.setText(null);

            // 2. Show relevant UI
            this.usernameTextField.setDisable(false);
            this.hostTextField.setDisable(true);
            this.portTextField.setDisable(true);

            this.joinButton.setVisible(true);
            ValidationHandler.showError(this.errorLabel, new Response(true, null));

            System.out.println("Cancellation");
        });
        this.cancelButton.setOnAction((actionEvent) -> {
            joinWorker.cancel();
        });
    }

    // Helpers
    private void resetConnectionScreen() {
        this.usernameTextField.setDisable(false);
        this.hostTextField.setDisable(false);
        this.portTextField.setDisable(false);

        this.usernameTextField.setText(null);
        this.hostTextField.setText(null);
        this.portTextField.setText(null);
        this.errorLabel.setVisible(false);

        this.joinButton.setVisible(false);
        this.cancelButton.setVisible(false);
        this.loadingProgressIndicator.setVisible(false);
        this.loadingLabel.setText(null);

        super.submitButton.setVisible(true);
    }

    private void addChat(String username) {
        this.rootController.addChatSession(this.socket, username);
        this.resetConnectionScreen();
    }
}
