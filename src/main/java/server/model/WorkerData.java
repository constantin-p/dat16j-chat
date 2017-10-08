package server.model;

import util.ProtocolHandler;

import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;

public class WorkerData {
    public PrintWriter out;
    private Instant lastActiveTimestamp;
    private Runnable close;

    public WorkerData(PrintWriter out, Runnable close) {
        this.out = out;
        this.close = close;

        this.markAsActive();
    }

    public void markAsActive() {
        this.lastActiveTimestamp = Instant.now();
    }

    public boolean isActive() {
        System.out.println("Check active: " + Duration.between(this.lastActiveTimestamp, Instant.now()) +
        " - " + ProtocolHandler.DEFAULT_ACTIVE_CHECK_INTERVAL
                .plus(ProtocolHandler.DEFAULT_ACTIVE_CHECK_DELAY) + " : "
        + ((Duration.between(this.lastActiveTimestamp, Instant.now())
                .compareTo(ProtocolHandler.DEFAULT_ACTIVE_CHECK_INTERVAL
                        .plus(ProtocolHandler.DEFAULT_ACTIVE_CHECK_DELAY))) <= 0));

        return (Duration.between(this.lastActiveTimestamp, Instant.now())
                .compareTo(ProtocolHandler.DEFAULT_ACTIVE_CHECK_INTERVAL
                        .plus(ProtocolHandler.DEFAULT_ACTIVE_CHECK_DELAY))) <= 0;
    }

    public void closeWorker() {
        this.close.run();
    }
}
