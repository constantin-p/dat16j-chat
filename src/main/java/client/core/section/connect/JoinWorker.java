package client.core.section.connect;

import client.model.ClientSocketData;
import javafx.concurrent.Task;
import util.ProtocolHandler;
import util.Response;
import util.ValidationHandler;

import java.io.IOException;

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

    protected JoinWorker(ClientSocketData clientSocketData, String username,
                         StartCallback startCallback, SuccessCallback successCallback, FailureCallback failureCallback, CancellationCallback cancellationCallback) {

        startCallback.call();
        this.task = new JoinTask(clientSocketData, username, successCallback, failureCallback, cancellationCallback);
        new Thread(this.task).start();
    }

    protected boolean cancel() {
        if (this.task != null) {
            return this.task.cancel(false);
        } else {
            return false;
        }
    }

    private class JoinTask extends Task<Response> {
        private ClientSocketData clientSocketData;
        private String username;

        private SuccessCallback successCallback;
        private FailureCallback failureCallback;
        private CancellationCallback cancellationCallback;

        protected JoinTask(ClientSocketData clientSocketData, String username,
                           SuccessCallback successCallback, FailureCallback failureCallback, CancellationCallback cancellationCallback) {
            this.clientSocketData = clientSocketData;
            this.username = username;

            this.successCallback = successCallback;
            this.failureCallback = failureCallback;
            this.cancellationCallback = cancellationCallback;
        }

        // TODO: handle server shutdown
        @Override
        protected Response call() throws IOException {

            this.clientSocketData.out.println(ProtocolHandler.Command.Format.join(this.username));

            // Listen for the server response
            String message = this.clientSocketData.in.readLine();
            if (message.startsWith(ProtocolHandler.Command.STATUS_OK)) {
                return new Response(true, this.username);
            } else if (message.startsWith(ProtocolHandler.Command.STATUS_ERROR)) {
                String payload = ProtocolHandler.getPayload(message, ProtocolHandler.Command.STATUS_ERROR.length());
                String[] payloadParts = payload.split(":");
                if (payloadParts.length < 2 || payloadParts[0].isEmpty()) {
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
