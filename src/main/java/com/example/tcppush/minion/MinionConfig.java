package com.example.tcppush.minion;

import java.util.List;

public record MinionConfig(
        String imei,
        String host,
        int port,
        List<String> messages,
        long waitTimeMs,
        boolean loop,
        int connectTimeoutMs) {

    public MinionConfig {
        messages = List.copyOf(messages);
    }

    public String endpoint() {
        return host + ":" + port;
    }
}
