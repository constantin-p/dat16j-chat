package client.core.section;

import client.core.RootController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import util.ValidationHandler;

public class ConnectController extends SectionBaseController implements UISection {
    private static final String TEMPLATE_PATH = "templates/section/connect.fxml";

    private RootController rootController;

    @FXML
    private Label errorLabel;

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

    }
}
