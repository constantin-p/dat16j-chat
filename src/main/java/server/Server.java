package server;

import server.model.WorkerData;
import util.ProtocolHandler;
import util.ValidationHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private final HashMap<String, WorkerData> store = new HashMap<>();
    private int workerIndex = 0;

    protected Server(int port) throws IOException {
        Timer activeUsersWorker = null;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("[SERVER]       : Listening ...");

            // Start a scheduled task that removes inactive users from the server
            activeUsersWorker = this.startActiveUsersWorker();

            while (true) {
                // Create a new worker for each connection we establish
                Socket socket = serverSocket.accept();
                System.out.println("[SERVER]       : New connection established");
                new Worker(socket, ++this.workerIndex).start();
            }
        } catch (IOException exception) {
            throw exception;
        } finally {
            if (activeUsersWorker != null) {
                activeUsersWorker.cancel();
            }
        }
    }

    private Timer startActiveUsersWorker() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (store) {
                    boolean updateUserList = false;
                    for (Map.Entry<String, WorkerData> entry : store.entrySet()) {
                        WorkerData workerData = entry.getValue();
                        if (!workerData.isActive()) {
                            workerData.closeWorker();
                            updateUserList = true;
                        }
                    }
                    if (updateUserList) {
                        sendUserList();
                    }
                }
            }
        }, 0, ProtocolHandler.DEFAULT_ACTIVE_CHECK_INTERVAL.toMillis());

        return timer;
    }

    private void sendUserList() {
        synchronized (store) {
            Set<String> keys = store.keySet();
            String[] userList = keys.toArray(new String[keys.size()]);

            for (Map.Entry<String, WorkerData> entry : store.entrySet()) {
                entry.getValue().out.println(ProtocolHandler.Command.Format.list(String.join(" ", userList)));
            }
        }
    }

    private class Worker extends Thread {
        private boolean shutdown = false;

        private int order;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        private String username;

        public Worker(Socket socket, int order) {
            this.socket = socket;
            this.order = order;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                System.out.println("[SERVER][WORKER: " + this.order + "]: Active");

                while (!this.shutdown) { // listen indefinitely (up until QUIT, or shutdown) for commands
                    String message = in.readLine();

                    if (message == null) {
                        System.out.println("[SERVER][WORKER: " + this.order + "]: NULL received");
                        this.close();
                    } else {
                        System.out.println("[SERVER][WORKER: " + this.order + "]: Message received: " + message);

                        if (message.startsWith(ProtocolHandler.Command.JOIN)) {
                            String payload = ProtocolHandler.getPayload(message, ProtocolHandler.Command.JOIN.length());
                            // Handle the scenario when:
                            //      JOIN <user_name>, <server_ip>: <server_port>
                            // is used, instead of:
                            //      JOIN <user_name> 
                            if (payload.contains(",")) {
                                String[] payloadParts = payload.split(",");
                                payload = payloadParts[0];
                            }

                            if (payload.length() > ProtocolHandler.Validation.Rule.Username.MAX_LENGTH) {
                                // username invalid (reason: exceeds max length)
                                System.out.println("[SERVER][WORKER: " + this.order + "]:     -> ✘ " +
                                        ProtocolHandler.Command.Format.usernameExceedsLengthStatusError());

                                out.println(ProtocolHandler.Command.Format.usernameExceedsLengthStatusError());
                            } else if (!payload.matches(ProtocolHandler.Validation.Rule.Username.VALID_CHAR)) {
                                // username invalid (reason: contains invalid characters)
                                System.out.println("[SERVER][WORKER: " + this.order + "]:     -> ✘ " +
                                        ProtocolHandler.Command.Format.usernameInvalidCharStatusError());

                                out.println(ProtocolHandler.Command.Format.usernameInvalidCharStatusError());
                            } else {
                                // username string valid, check is the username is already taken
                                synchronized (store) {
                                    if (!store.containsKey(payload)) {
                                        System.out.println("[SERVER][WORKER: " + this.order + "]:     -> ✔ Username (" +
                                                payload + ") is valid");

                                        out.println(ProtocolHandler.Command.STATUS_OK);
                                        store.put(payload, new WorkerData(out, () -> {
                                            this.requestClose();
                                        }));

                                        // Send the updated user list
                                        sendUserList();
                                        this.username = payload;
                                    } else {
                                        System.out.println("[SERVER][WORKER: " + this.order + "]:     -> ✘ " +
                                                ProtocolHandler.Command.Format.usernameTakenStatusError());

                                        out.println(ProtocolHandler.Command.Format.usernameTakenStatusError());
                                    }
                                }
                            }
                        } else if (message.startsWith(ProtocolHandler.Command.DATA)) {
                            String payload = ProtocolHandler.getPayload(message, ProtocolHandler.Command.DATA.length());

                            // Validate payload structure
                            String[] payloadParts = payload.split(":");
                            if (payloadParts.length < 2 || payloadParts[0].isEmpty()) {
                                System.out.println("[SERVER][WORKER: " + this.order + "]:     -> ✘ " +
                                        ValidationHandler.Error.INVALID_CLIENT_MESSAGE + ": " + message);
                            } else {
                                // The text message can contain ':', so reconstruct the message and check if valid
                                String textMessage = ProtocolHandler.getPayload(payload, payloadParts[0].length() + 1);
                                if (ValidationHandler.validateMessage(textMessage).success) {
                                    synchronized (store) {
                                        for (Map.Entry<String, WorkerData> entry : store.entrySet()) {
                                            entry.getValue().out.println(message);
                                        }
                                    }
                                }
                            }
                        } else if (message.startsWith(ProtocolHandler.Command.ALIVE)) {
                            synchronized (store) {
                                store.get(this.username).markAsActive();
                            }
                        } else if (message.startsWith(ProtocolHandler.Command.QUIT)) {
                            this.requestClose();
                        } else {
                            System.out.println("[SERVER][WORKER: " + this.order + "]: Unrecognized command!\n\t\t " + message);
                        }
                    }
                }

            } catch (IOException exception) {
                exception.printStackTrace();
            } finally {
                System.out.println("[SERVER][WORKER: " + this.order + "]: Closing");
                // remove data
                if (this.username != null) {
                    synchronized (store) {
                        store.remove(this.username);

                        // Send the updated user list
                        sendUserList();
                    }
                }
            }
        }

        // https://stackoverflow.com/questions/155243/why-is-it-impossible-without-attempting-i-o-to-detect-that-tcp-socket-was-grac?rq=1
        private void requestClose() {
            this.shutdown = true;
            try {
                // Sends the 'FIN' on the network
                socket.shutdownOutput();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        private void close() {
            this.shutdown = true;
            try {
                socket.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
