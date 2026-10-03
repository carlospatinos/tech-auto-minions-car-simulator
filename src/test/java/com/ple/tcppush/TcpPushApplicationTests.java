package com.ple.tcppush;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.ple.tcppush.minion.MinionManager;
import com.ple.tcppush.minion.MinionState;

@SpringBootTest(properties = {
        "minions.count=2",
        "minions.auto-start=false",
        "minions.defaults.wait-time-ms=50",
        "minions.instances[0].imei=111111111111111"
})
class TcpPushApplicationTests {

    private static final List<String> received = new CopyOnWriteArrayList<>();
    private static final ServerSocket server;

    static {
        try {
            server = new ServerSocket(0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        Thread acceptor = new Thread(() -> {
            while (!server.isClosed()) {
                try {
                    Socket client = server.accept();
                    new Thread(() -> read(client)).start();
                } catch (IOException ignored) {
                }
            }
        });
        acceptor.setDaemon(true);
        acceptor.start();
    }

    private static void read(Socket client) {
        try (client; var in = new BufferedReader(
                new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                received.add(line);
            }
        } catch (IOException ignored) {
        }
    }

    @DynamicPropertySource
    static void tcpPort(DynamicPropertyRegistry registry) {
        registry.add("minions.defaults.port", server::getLocalPort);
    }

    @AfterAll
    static void close() throws IOException {
        server.close();
    }

    @Autowired
    MinionManager manager;

    @Test
    void minionsPushAllMessages() throws InterruptedException {
        assertThat(manager.startAll()).isEqualTo(2);

        long deadline = System.currentTimeMillis() + 5000;
        while ((received.size() < 6 || manager.statuses().stream().anyMatch(s -> s.alive()))
                && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }

        assertThat(manager.statuses()).allMatch(s -> s.state() == MinionState.COMPLETED);
        assertThat(received).hasSize(6).contains("HELLO from 111111111111111");
    }
}
