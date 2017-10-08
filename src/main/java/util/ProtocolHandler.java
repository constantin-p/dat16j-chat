package util;

import java.time.Duration;

public class ProtocolHandler {
    public static final int DEFAULT_PORT = 9567;
    public static final Duration DEFAULT_ACTIVE_CHECK_INTERVAL = Duration.ofMinutes(1);
    public static final Duration DEFAULT_ACTIVE_CHECK_DELAY = Duration.ofSeconds(30); // used to account for network latency

    public static class Command {
        public static final String JOIN = "JOIN";
        public static final String DATA = "DATA";
        public static final String ALIVE = "IMAV";
        public static final String LIST = "LIST";
        public static final String QUIT = "QUIT";
        public static final String STATUS_OK = "J_OK";
        public static final String STATUS_ERROR = "J_ER";

        public static class Format {
            public static final String JOIN = "JOIN %s"; // username
            public static final String DATA = "DATA %s: %s"; // username, text
            public static final String LIST = "LIST %s"; // username, text
            public static final String STATUS_ERROR = "J_ER %d:%s";

            public static String join(String username) {
                return String.format(JOIN, username);
            }

            public static String data(String username, String message) {
                return String.format(DATA, username, message);
            }

            public static String list(String userList) {
                return String.format(LIST, userList);
            }

            public static String usernameExceedsLengthStatusError() {
                return String.format(STATUS_ERROR, Error.Code.USERNAME_EXCEEDS_LENGTH, Error.USERNAME_EXCEEDS_LENGTH);
            }

            public static String usernameInvalidCharStatusError() {
                return String.format(STATUS_ERROR, Error.Code.USERNAME_INVALID_CHAR, Error.USERNAME_INVALID_CHAR);
            }

            public static String usernameTakenStatusError() {
                return String.format(STATUS_ERROR, Error.Code.USERNAME_TAKEN, Error.USERNAME_TAKEN);
            }
        }
    }

    public static class Error {
        public static final String USERNAME_EXCEEDS_LENGTH = "Invalid username! Reason: exceeds max length.";
        public static final String USERNAME_INVALID_CHAR = "Invalid username! Reason: contains invalid characters.";
        public static final String USERNAME_TAKEN = "Invalid username! Reason: already taken.";

        public static class Code {
            public static final int USERNAME_EXCEEDS_LENGTH = 1;
            public static final int USERNAME_INVALID_CHAR = 2;
            public static final int USERNAME_TAKEN = 3;
        }
    }

    public static class Validation {

        public static class Rule {
            public static class Username {
                public static final int MAX_LENGTH = 12;
                public static final String VALID_CHAR = "^[a-zA-Z0-9_-]*$"; // a-z, A-Z, 0-9, _ , -
            }

            public static class Port {
                public static final String VALID_CHAR = "^[0-9]+$";
            }

            public static class Message {
                public static final int MAX_LENGTH = 250;
            }
        }
    }

    public static String getPayload(String message, int commandLength) {
        return message.substring(commandLength).trim();
    }
}