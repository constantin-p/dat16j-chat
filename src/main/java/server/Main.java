package server;

import server.model.ServerData;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("[SERVER]    CLI: Starting on port: " + ServerData.DEFAULT_PORT);

        new Server(ServerData.DEFAULT_PORT);
    }
}
