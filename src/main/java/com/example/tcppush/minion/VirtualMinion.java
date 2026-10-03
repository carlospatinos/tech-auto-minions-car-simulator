package com.example.tcppush.minion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A virtual device that connects to a TCP endpoint and pushes its messages one by one,
 * waiting {@code waitTimeMs} between each. Each run happens in its own dedicated thread.
 */
public class VirtualMinion {

    private static final Logger log = LogManager.getLogger(VirtualMinion.class);

    private final MinionConfig config;
    private final AtomicInteger sentCount = new AtomicInteger();

    private volatile MinionState state = MinionState.CREATED;
    private volatile Thread thread;
    private volatile Socket socket;
    private volatile boolean stopRequested;
    private volatile String lastError;
    private volatile Instant lastMessageAt;

    public VirtualMinion(MinionConfig config) {
        this.config = config;
    }

    public synchronized boolean start() {
        if (isAlive()) {
            log.warn("Minion {} is already running", config.imei());
            return false;
        }
        stopRequested = false;
        lastError = null;
        sentCount.set(0);
        thread = new Thread(this::run, "minion-" + config.imei());
        thread.start();
        return true;
    }

    public synchronized boolean stop() {
        if (!isAlive()) {
            return false;
        }
        stopRequested = true;
        state = MinionState.STOPPED;
        closeSocket();
        thread.interrupt();
        log.info("Stop requested for minion {}", config.imei());
        return true;
    }

    public boolean isAlive() {
        Thread t = thread;
        return t != null && t.isAlive();
    }

    private void run() {
        ThreadContext.put("imei", config.imei());
        try {
            state = MinionState.CONNECTING;
            log.info("Minion {} connecting to {}", config.imei(), config.endpoint());
            try (Socket s = new Socket()) {
                socket = s;
                s.connect(new InetSocketAddress(config.host(), config.port()), config.connectTimeoutMs());
                state = MinionState.RUNNING;
                log.info("Minion {} connected to {}", config.imei(), config.endpoint());

                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
                do {
                    sendAll(writer);
                } while (config.loop() && !stopRequested);
            }
            if (!stopRequested) {
                state = MinionState.COMPLETED;
                log.info("Minion {} finished sending {} message(s)", config.imei(), sentCount.get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Minion {} interrupted", config.imei());
        } catch (IOException e) {
            if (stopRequested) {
                log.info("Minion {} connection closed on stop", config.imei());
            } else {
                state = MinionState.FAILED;
                lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error("Minion {} TCP error with {}: {}", config.imei(), config.endpoint(), lastError, e);
            }
        } catch (RuntimeException e) {
            state = MinionState.FAILED;
            lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error("Minion {} unexpected error", config.imei(), e);
        } finally {
            socket = null;
            if (stopRequested) {
                state = MinionState.STOPPED;
                log.info("Minion {} stopped after sending {} message(s)", config.imei(), sentCount.get());
            }
            ThreadContext.remove("imei");
        }
    }

    private void sendAll(BufferedWriter writer) throws IOException, InterruptedException {
        var messages = config.messages();
        for (int i = 0; i < messages.size() && !stopRequested; i++) {
            String payload = messages.get(i).replace("{imei}", config.imei());
            writer.write(payload);
            writer.newLine();
            writer.flush();
            sentCount.incrementAndGet();
            lastMessageAt = Instant.now();
            log.info("Minion {} sent message {}/{} to {}: {}",
                    config.imei(), i + 1, messages.size(), config.endpoint(), payload);

            boolean more = i < messages.size() - 1 || config.loop();
            if (more && config.waitTimeMs() > 0) {
                Thread.sleep(config.waitTimeMs());
            }
        }
    }

    private void closeSocket() {
        Socket s = socket;
        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                log.warn("Minion {} error closing socket: {}", config.imei(), e.getMessage());
            }
        }
    }

    public MinionStatus status() {
        return new MinionStatus(config.imei(), config.endpoint(), state, isAlive(),
                sentCount.get(), config.messages().size(), config.waitTimeMs(), lastMessageAt, lastError);
    }

    public MinionConfig getConfig() {
        return config;
    }

    public MinionState getState() {
        return state;
    }
}
