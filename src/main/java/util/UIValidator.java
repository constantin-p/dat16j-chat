package util;

import javafx.scene.control.Control;
import javafx.scene.control.Label;

public class UIValidator {

    public static boolean validateControl(Control control, Label errorLabel, Response validation) {
        if (showError(errorLabel, validation)) {
            // control.setStyle( "-fx-text-fill: #FF3B30;");
            return true;
        } else {
            // control.setStyle( "-fx-text-fill: #FF3B30;");
            return false;
        }
    }


    public static boolean showError(Label errorLabel, Response validation) {
        if (validation.success) {
            errorLabel.setVisible(false);
            return true;
        } else {
            errorLabel.setText(validation.msg);
            errorLabel.setVisible(true);
            return false;
        }
    }
}
