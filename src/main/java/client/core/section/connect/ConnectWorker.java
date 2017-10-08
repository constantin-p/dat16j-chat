package client.core.section.connect;

import client.model.ClientSocketData;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConnectWorker {
    private ConnectTask task;

    protected interface StartCallback {
        void call();
    };

    protected interface SuccessCallback {
        void call(ClientSocketData clientSocketData);
    };

    protected interface FailureCallback {
        void call();
    };

    protected interface CancellationCallback {
        void call();
    };

    public ConnectWorker(String host, int port,
                       StartCallback startCallback, SuccessCallback successCallback, FailureCallback failureCallback, CancellationCallback cancellationCallback) {

        startCallback.call();
        this.task = new ConnectTask(host, port, successCallback, failureCallback, cancellationCallback);
        new Thread(this.task).start();
    }

    public boolean cancel() {
        if (this.task != null) {
            return this.task.cancelTask(false);
        } else {
            return false;
        }
    }

    private class ConnectTask extends Task<ClientSocketData> {
        private InetSocketAddress address;
        private Socket socket = new Socket();

        private SuccessCallback successCallback;
        private FailureCallback failureCallback;
        private CancellationCallback cancellationCallback;

        protected ConnectTask(String host, int port,
                              SuccessCallback successCallback, FailureCallback failureCallback, CancellationCallback cancellationCallback) {
            this.address = new InetSocketAddress(host, port);
            this.successCallback = successCallback;
            this.failureCallback = failureCallback;
            this.cancellationCallback = cancellationCallback;
        }

        @Override
        protected ClientSocketData call() throws IOException {
            this.socket.connect(address);
            return new ClientSocketData(this.socket);
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            System.out.println("[CONNECT_TASK]: Finished!");
            this.successCallback.call(this.getValue());
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            System.out.println("[CONNECT_TASK]: Cancelled!");
            this.cancellationCallback.call();
        }

        @Override
        protected void failed() {
            super.failed();
            System.out.println("[CONNECT_TASK]: Failed!");
            this.failureCallback.call();
        }

        protected boolean cancelTask(boolean mayInterruptIfRunning) {
            try {
                this.socket.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            } finally {
                return super.cancel(mayInterruptIfRunning);
            }
        }
    }
}
