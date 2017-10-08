package client.core.section.session;

import client.model.ClientSocketData;
import client.model.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import util.ProtocolHandler;
import util.ValidationHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class SessionWorker extends Thread {
    private boolean shutdown = false;

    private int index;
    private ClientSocketData clientSocketData;
    protected String username;

    private ObservableList<Message> messageList = FXCollections.observableArrayList();;
    private ObservableList<String> userList = FXCollections.observableArrayList();;
    private Runnable onDisconnect = () -> {};

    public SessionWorker(int index, ClientSocketData clientSocketData, String username) throws IOException {
        this.index = index;
        this.clientSocketData = clientSocketData;
        this.username = username;
    }

    protected void addLists(ObservableList<Message> messageList, ObservableList<String> userList) {
        this.messageList = messageList;
        this.userList = userList;
    }

    protected void setOnDisconnect(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }

    protected void sendMessage(String message) {
        if (this.clientSocketData.out != null) {
            this.clientSocketData.out.println(ProtocolHandler.Command.Format.data(this.username, message));
        }
    }

    public void disconnect() {
        if (this.clientSocketData.out != null) {
            this.clientSocketData.out.println(ProtocolHandler.Command.QUIT);
            this.requestClose();
        }
    }

    private Timer startActivePingWorker() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (clientSocketData.out != null) {
                    clientSocketData.out.println(ProtocolHandler.Command.ALIVE);
                }
            }
        }, 0, ProtocolHandler.DEFAULT_ACTIVE_CHECK_INTERVAL.toMillis());

        return timer;
    }

    @Override
    public void run() {
        Timer sendAlivePingWorker = this.startActivePingWorker();
        try {
            while (!this.shutdown) { // listen indefinitely for commands
                String message = this.clientSocketData.in.readLine();
                if (message == null) {
                    System.out.println("[SESSION][WORKER: " + this.index + "]: NULL received");
                    this.close();
                } else {
                    System.out.println("[SESSION][WORKER: " + this.index + "]: Message received: " + message);

                    if (message.startsWith(ProtocolHandler.Command.DATA)) {
                        String payload = ProtocolHandler.getPayload(message, ProtocolHandler.Command.DATA.length());

                        // Validate payload structure
                        String[] payloadParts = payload.split(":");
                        if (payloadParts.length < 2 || payloadParts[0].isEmpty()) {
                            System.out.println("[SESSION][WORKER: " + this.index + "]:     -> ✘ " +
                                    ValidationHandler.Error.INVALID_SERVER_MESSAGE + ": " + message);
                        } else {
                            // The text message can contain ':', so reconstruct the message
                            String textMessage = ProtocolHandler.getPayload(payload, payloadParts[0].length() + 1);
                            if (ValidationHandler.validateMessage(textMessage).success) {
                                // Valid message, add it to the list
                                Platform.runLater(() -> this.messageList.add(new Message(payloadParts[0], textMessage)));
                            }
                        }
                    } else if (message.startsWith(ProtocolHandler.Command.LIST)) {
                        String payload = ProtocolHandler.getPayload(message, ProtocolHandler.Command.DATA.length());

                        // Validate payload structure (username validity)
                        String[] payloadParts = Arrays.stream(payload.split(" "))
                                .map((item) -> item.trim())
                                .filter((item) -> ValidationHandler.validateUsername(item).success)
                                .toArray(size -> new String[size]);

                        Platform.runLater(() -> this.userList.setAll(payloadParts));
                    } else {
                        System.out.println("[SESSION][WORKER: " + this.index + "]: Unrecognized command!\n\t\t " + message);
                    }
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            sendAlivePingWorker.cancel();
        }
    }

    private void requestClose() {
        this.shutdown = true;
        try {
            // Sends the 'FIN' on the network
            this.clientSocketData.socket.shutdownOutput();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void close() {
        this.shutdown = true;
        this.onDisconnect.run();
        try {
            this.clientSocketData.socket.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public String getSessionName() {
        String name = "";
        for (int i = 0; i <= this.index / 26; i++) {
            if (this.index / 26 == i) {
                name += (char) ('A' + (this.index % 26));
            } else {
                name += (char) ('A' + i);
            }
        }
        return name;
    }
}
