package client.core.section;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SectionBaseController {

    @FXML
    private Button submitButton;
    protected BooleanProperty isDisabled = new SimpleBooleanProperty(false);

    @FXML
    public void initialize() {
        submitButton.disableProperty().bind(isDisabled);
    }
}
