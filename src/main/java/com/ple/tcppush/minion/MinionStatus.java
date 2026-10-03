package com.ple.tcppush.minion;

import java.time.Instant;

public record MinionStatus(
        String imei,
        String endpoint,
        MinionState state,
        boolean alive,
        int messagesSent,
        int totalMessages,
        long waitTimeMs,
        Instant lastMessageAt,
        String lastError) {
}
