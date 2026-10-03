package com.example.tcppush.health;

import com.example.tcppush.minion.MinionManager;
import com.example.tcppush.minion.MinionState;
import com.example.tcppush.minion.MinionStatus;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Exposed under /actuator/health as the "minions" component. */
@Component("minions")
public class MinionsHealthIndicator implements HealthIndicator {

    private final MinionManager manager;

    public MinionsHealthIndicator(MinionManager manager) {
        this.manager = manager;
    }

    @Override
    public Health health() {
        List<MinionStatus> statuses = manager.statuses();
        Map<MinionState, Long> byState = statuses.stream()
                .collect(Collectors.groupingBy(MinionStatus::state, Collectors.counting()));
        Map<String, String> failed = statuses.stream()
                .filter(s -> s.state() == MinionState.FAILED)
                .collect(Collectors.toMap(MinionStatus::imei, s -> String.valueOf(s.lastError())));

        Health.Builder builder = failed.isEmpty() ? Health.up() : Health.status("DEGRADED");
        return builder
                .withDetail("total", statuses.size())
                .withDetail("alive", statuses.stream().filter(MinionStatus::alive).count())
                .withDetail("byState", byState)
                .withDetail("failed", failed)
                .build();
    }
}
