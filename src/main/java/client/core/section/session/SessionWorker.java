package client.core.section.session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SessionWorker extends Thread {
    private int index;
    private Socket socket;
    private String username;

    private BufferedReader in;
    private PrintWriter out;

    public SessionWorker(Socket socket, String username, int index) throws IOException {
        this.index = index;
        this.socket = socket;
        this.username = username;

        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        while (true) { // listen indefinitely for commands

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
