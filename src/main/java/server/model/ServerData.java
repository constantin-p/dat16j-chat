package server.model;

public class ServerData {
    public static final int DEFAULT_PORT = 9567;

    public static class Command {
        public static final String JOIN = "JOIN";
        public static final String DATA = "DATA";
        public static final String ALIVE = "IMAV";
        public static final String QUIT = "QUIT";
        public static final String STATUS_OK = "J_OK";
        public static final String STATUS_ERROR = "J_ER %d:%s";

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
        public static final String USERNAME_REGEX = "^[a-zA-Z0-9_-]*$"; // a-z, A-Z, 0-9, _ ,  -
        public static final int USERNAME_MAX_LENGTH = 12;
    }
}