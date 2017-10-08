package util;

import javafx.scene.control.Control;
import javafx.scene.control.Label;

import java.net.URI;
import java.net.URISyntaxException;

public class ValidationHandler {

    public static class Error {
        public static final String CONNECTION = "Error connecting to the server";
        public static final String INVALID_SERVER_RESPONSE = "Invalid server response";
        public static final String INVALID_SERVER_MESSAGE = "Invalid server message";
        public static final String INVALID_CLIENT_MESSAGE = "Invalid client message";

        public static class Username {
            public static final String REQUIRED = "Username required";
            public static final String EXCEEDS_LENGTH = "Username exceeds max length (12 chars)";
            public static final String INVALID_CHAR = "Invalid username (a-z, A-Z, 0-9, _ , - )";
        }

        public static class Host {
            public static final String REQUIRED = "Host required";
            public static final String INVALID = "Host invalid";
        }

        public static class Port {
            public static final String REQUIRED = "Port required";
            public static final String INVALID = "Port invalid";
        }

        public static class Message {
            public static final String REQUIRED = "Message required";
            public static final String EXCEEDS_LENGTH = "Message exceeds max length (250 chars)";
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
        } else if (username.length() > ProtocolHandler.Validation.Rule.Username.MAX_LENGTH) {
            return new Response(false, Error.Username.EXCEEDS_LENGTH);
        } else if (!username.matches(ProtocolHandler.Validation.Rule.Username.VALID_CHAR)) {
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
        } else if (!port.matches(ProtocolHandler.Validation.Rule.Port.VALID_CHAR)) {
            return new Response(false, Error.Port.INVALID);
        }
        return new Response(true);
    }

    public static Response validateMessage(String message) {
        if(message == null || message.isEmpty()) {
            return new Response(false, Error.Message.REQUIRED);
        } else if (message.length() > ProtocolHandler.Validation.Rule.Message.MAX_LENGTH) {
            return new Response(false, Error.Message.EXCEEDS_LENGTH);
        }
        return new Response(true);
    }
}
