package server;

import util.ProtocolHandler;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("[SERVER]    CLI: Starting on port: " + ProtocolHandler.DEFAULT_PORT);

        new Server(ProtocolHandler.DEFAULT_PORT);
    }
}
