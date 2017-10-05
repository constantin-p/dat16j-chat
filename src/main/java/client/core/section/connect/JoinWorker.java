package client.core.section.connect;

import javafx.concurrent.Task;
import util.ProtocolHandler;
import util.Response;
import util.ValidationHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class JoinWorker {
    private JoinTask task;

    protected interface StartCallback {
        void call();
    };

    protected interface SuccessCallback {
        void call(Response response);
    };

    protected interface FailureCallback {
        void call();
    };

    protected interface CancellationCallback {
        void call();
    };

    public JoinWorker(Socket socket, String username,
                      StartCallback startCallback, SuccessCallback successCallback, FailureCallback failureCallback, CancellationCallback cancellationCallback) {

        startCallback.call();
        this.task = new JoinTask(socket, username, successCallback, failureCallback, cancellationCallback);
        new Thread(this.task).start();
    }

    public boolean cancel() {
        if (this.task != null) {
            return this.task.cancel(false);
        } else {
            return false;
        }
    }

    private class JoinTask extends Task<Response> {
        private Socket socket;
        private String username;

        private SuccessCallback successCallback;
        private FailureCallback failureCallback;
        private CancellationCallback cancellationCallback;

        protected JoinTask(Socket socket, String username,
                           SuccessCallback successCallback, FailureCallback failureCallback, CancellationCallback cancellationCallback) {
            this.socket = socket;
            this.username = username;

            this.successCallback = successCallback;
            this.failureCallback = failureCallback;
            this.cancellationCallback = cancellationCallback;
        }

        @Override
        protected Response call() throws IOException {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);

            out.println(ProtocolHandler.Command.Format.join(this.username));

            // Listen for the server response
            String message = in.readLine();
            if (message.startsWith(ProtocolHandler.Command.STATUS_OK)) {
                return new Response(true, this.username);
            } else if (message.startsWith(ProtocolHandler.Command.STATUS_ERROR)) {
                String payload = ProtocolHandler.getPayload(message, ProtocolHandler.Command.STATUS_ERROR.length());
                String[] payloadParts = payload.split(":");
                if (payloadParts.length < 2 && (payloadParts[0].isEmpty() || payloadParts[1].isEmpty())) {
                    System.out.println("[CLIENT]: Username rejected!\n\t\t " +
                            ValidationHandler.Error.INVALID_SERVER_RESPONSE + ": " + message);
                    return new Response(false, ValidationHandler.Error.INVALID_SERVER_RESPONSE);
                }
                // The error message can contain ':', so reconstruct the message
                String errorMessage = ProtocolHandler.getPayload(payload, payloadParts[0].length() + 1);

                System.out.println("[CLIENT]: Username rejected!\n\t\t " +
                    "[ERROR CODE: " + payloadParts[0] + "]" +
                    "[ERROR MSG: " + errorMessage + "]");

                return new Response(false, errorMessage);
            } else {
                System.out.println("[CLIENT]: Unrecognized command!\n\t\t " + message);
                return new Response(false, ValidationHandler.Error.INVALID_SERVER_RESPONSE);
            }
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            System.out.println("[JOIN_TASK]: Finished!");
            this.successCallback.call(this.getValue());
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            System.out.println("[JOIN_TASK]: Cancelled!");
            this.cancellationCallback.call();
        }

        @Override
        protected void failed() {
            super.failed();
            System.out.println("[JOIN_TASK]: Failed!");
            this.failureCallback.call();
        }
    }
}
