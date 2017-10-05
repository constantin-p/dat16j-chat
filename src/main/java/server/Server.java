package server;

import server.model.WorkerData;
import util.ProtocolHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private HashMap<String, WorkerData> store = new HashMap<>();
    private int workerIndex = 0;

    protected Server(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("[SERVER]       : Listening ...");

        while (true) {
            // Create a new worker for each connection we establish
            Socket socket = serverSocket.accept();
            System.out.println("[SERVER]       : New connection established");
            new Worker(socket, ++this.workerIndex).start();
        }
    }

    private class Worker extends Thread {

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

                while (true) { // listen indefinitely (up until QUIT, or connection drop) for commands
                    String message = in.readLine();
                    System.out.println("[SERVER][WORKER: " + this.order + "]: Message received: " + message);

                    if (message.startsWith(ProtocolHandler.Command.JOIN)) {
                        String payload = ProtocolHandler.getPayload(message, ProtocolHandler.Command.JOIN.length());

                        if (payload.length() > ProtocolHandler.Validation.USERNAME_MAX_LENGTH) {
                            // username invalid (reason: exceeds max length)
                            System.out.println("[SERVER][WORKER: " + this.order + "]:     -> ✘ " +
                                    ProtocolHandler.Command.Format.usernameExceedsLengthStatusError());

                            out.println(ProtocolHandler.Command.Format.usernameExceedsLengthStatusError());
                        } else if (!payload.matches(ProtocolHandler.Validation.USERNAME_REGEX)) {
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
                                    store.put(payload, new WorkerData(out));
                                } else {
                                    System.out.println("[SERVER][WORKER: " + this.order + "]:     -> ✘ " +
                                            ProtocolHandler.Command.Format.usernameTakenStatusError());

                                    out.println(ProtocolHandler.Command.Format.usernameTakenStatusError());
                                }
                            }
                        }
                    } else if (message.startsWith(ProtocolHandler.Command.DATA)) {
                        String payload = ProtocolHandler.getPayload(message, ProtocolHandler.Command.DATA.length());

                        // TODO: Validate payload structure
                        synchronized (store) {
                            for (Map.Entry<String, WorkerData> entry : store.entrySet()) {
                                entry.getValue().out.println(message);
                            }
                        }
                    } else if (message.startsWith(ProtocolHandler.Command.ALIVE)) {
                        String payload = ProtocolHandler.getPayload(message, ProtocolHandler.Command.ALIVE.length());
                    } else if (message.startsWith(ProtocolHandler.Command.QUIT)) {
                        String payload = ProtocolHandler.getPayload(message, ProtocolHandler.Command.QUIT.length());
                    } else {
                        System.out.println("[SERVER][WORKER: " + this.order + "]: Unrecognized command!\n\t\t " + message);
                    }
                }

            } catch (IOException exception) {
                exception.printStackTrace();
            } finally {
                System.out.println("[SERVER][WORKER: " + this.order + "]: Closing");
                // remove data
                if (username != null) {
                    synchronized (store) {
                        store.remove(username);
                    }
                }
            }
        }
    }
}
