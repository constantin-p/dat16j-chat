package server;

import server.model.ServerData;
import server.model.WorkerData;

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

                System.out.println("[SERVER][WORKER:" + this.order + "]: Active");

                while (true) { // listen indefinitely (up until QUIT, or connection drop) for commands
                    String message = in.readLine();
                    System.out.println("[SERVER][WORKER:" + this.order + "]: Message received: " + message);

                    if (message.startsWith(ServerData.Command.JOIN)) {
                        String payload = this.getPayload(message, ServerData.Command.JOIN.length());

                        if (payload.length() > ServerData.Validation.USERNAME_MAX_LENGTH) {
                            // username invalid (reason: exceeds max length)
                            out.println(ServerData.Command.usernameExceedsLengthStatusError());
                        } else if (!payload.matches(ServerData.Validation.USERNAME_REGEX)) {
                            // username invalid (reason: contains invalid characters)
                            out.println(ServerData.Command.usernameInvalidCharStatusError());
                        } else {
                            // username string valid, check is the username is already taken
                            synchronized (store) {
                                if (!store.containsKey(payload)) {
                                    out.println(ServerData.Command.STATUS_OK);

                                    store.put(payload, new WorkerData(out));
                                } else {
                                    out.println(ServerData.Command.usernameTakenStatusError());
                                }
                            }
                        }
                    } else if (message.startsWith(ServerData.Command.DATA)) {
                        String payload = this.getPayload(message, ServerData.Command.DATA.length());

                        // TODO: Validate payload structure
                        synchronized (store) {
                            for (Map.Entry<String, WorkerData> entry : store.entrySet()) {
                                entry.getValue().out.println(message);
                            }
                        }
                    } else if (message.startsWith(ServerData.Command.ALIVE)) {
                        String payload = this.getPayload(message, ServerData.Command.ALIVE.length());
                    } else if (message.startsWith(ServerData.Command.QUIT)) {
                        String payload = this.getPayload(message, ServerData.Command.QUIT.length());
                    } else {
                        System.out.println("[SERVER][WORKER:" + this.order + "]: Unrecognized command!\n\t\t " + message);
                    }
                }

            } catch (IOException exception) {
                exception.printStackTrace();
            } finally {
                System.out.println("[SERVER][WORKER:" + this.order + "]: Closing");
                // remove data
                if (username != null) {
                    synchronized (store) {
                        store.remove(username);
                    }
                }
            }
        }

        private String getPayload(String message, int commandLength) {
            return message.substring(commandLength).trim();
        }
    }
}
