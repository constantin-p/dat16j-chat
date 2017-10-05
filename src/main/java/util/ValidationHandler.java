package util;

import javafx.scene.control.Control;
import javafx.scene.control.Label;

import java.net.URI;
import java.net.URISyntaxException;

public class ValidationHandler {

    public static class Error {
        public static class Username {
            public static final String REQUIRED = "Username required";
            public static final String EXCEEDS_LENGTH = "Username exceeds max length (12 chars)";
            public static final String INVALID_CHAR = "Invalid username (a-z, A-Z, 0-9, _ , - )";
            public static final String TAKEN = "Username already registered";
        }

        public static class Host {
            public static final String REQUIRED = "Host required";
            public static final String INVALID = "Host invalid";
        }

        public static class Port {
            public static final String REQUIRED = "Port required";
            public static final String INVALID = "Port invalid";
        }
    }

    public static class Rule {
        public static class Username {
            public static final int MAX_LENGTH = 12;
            public static final String VALID_CHAR = "^[a-zA-Z0-9_-]*$"; // a-z, A-Z, 0-9, _ , -
        }

        public static class Port {
            public static final String VALID_CHAR = "^[0-9]+$";
        }
    }

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


    public static Response validateUsername(String username) {
        if(username == null || username.isEmpty()) {
            return new Response(false, Error.Username.REQUIRED);
        } else if (username.length() > Rule.Username.MAX_LENGTH) {
            return new Response(false, Error.Username.EXCEEDS_LENGTH);
        } else if (!username.matches(Rule.Username.VALID_CHAR)) {
            return new Response(false, Error.Username.INVALID_CHAR);
        }
        return new Response(true);
    }

    public static Response validateHost(String host) {
        if(host == null || host.isEmpty()) {
            return new Response(false, Error.Host.REQUIRED);
        }
        try {
            URI uri = new URI("mock://" + host);

            if (uri.getHost() == null) {
                throw new URISyntaxException(uri.toString(), "URI must contain host");
            }
        } catch (URISyntaxException ex) {
            return new Response(false, Error.Host.INVALID);
        }
        return new Response(true);
    }

    public static Response validatePort(String port) {
        if(port == null || port.isEmpty()) {
            return new Response(false, Error.Port.REQUIRED);
        } else if (!port.matches(Rule.Port.VALID_CHAR)) {
            return new Response(false, Error.Port.INVALID);
        }
        return new Response(true);
    }
}
