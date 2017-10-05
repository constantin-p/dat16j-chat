package server.model;

import java.io.PrintWriter;

public class WorkerData {
    public PrintWriter out;
    public long lastActiveTimestamp;

    public WorkerData(PrintWriter out) {
        this.out = out;

        this.markAsActive();
    }

    public void markAsActive() {
        this.lastActiveTimestamp = System.currentTimeMillis();
    }
}
