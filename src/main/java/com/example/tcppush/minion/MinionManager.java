package com.example.tcppush.minion;

import com.example.tcppush.config.MinionProperties;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MinionManager {

    private static final Logger log = LogManager.getLogger(MinionManager.class);
    private static final int IMEI_LENGTH = 15;

    private final MinionProperties properties;
    private final Map<String, VirtualMinion> minions = new LinkedHashMap<>();

    public MinionManager(MinionProperties properties) {
        this.properties = properties;
        for (int i = 0; i < properties.getCount(); i++) {
            MinionConfig cfg = buildConfig(i);
            if (minions.putIfAbsent(cfg.imei(), new VirtualMinion(cfg)) != null) {
                throw new IllegalStateException("Duplicate minion IMEI: " + cfg.imei());
            }
        }
        log.info("Created {} virtual minion(s): {}", minions.size(), minions.keySet());
    }

    private MinionConfig buildConfig(int index) {
        MinionProperties.Defaults d = properties.getDefaults();
        MinionProperties.Override o = index < properties.getInstances().size()
                ? properties.getInstances().get(index)
                : new MinionProperties.Override();

        String imei = o.getImei() != null ? o.getImei() : generateImei(index);
        String host = o.getHost() != null ? o.getHost() : d.getHost();
        int port = o.getPort() != null ? o.getPort() : d.getPort();
        long wait = o.getWaitTimeMs() != null ? o.getWaitTimeMs() : d.getWaitTimeMs();
        List<String> messages = o.getMessages() != null && !o.getMessages().isEmpty()
                ? o.getMessages() : d.getMessages();

        return new MinionConfig(imei, host, port, messages, wait,
                properties.isLoop(), properties.getConnectTimeoutMs());
    }

    private String generateImei(int index) {
        String prefix = properties.getImeiPrefix();
        int padding = Math.max(1, IMEI_LENGTH - prefix.length());
        return prefix + String.format("%0" + padding + "d", index + 1);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (properties.isAutoStart()) {
            log.info("Auto-start enabled, starting all minions");
            startAll();
        }
    }

    public synchronized int startAll() {
        int started = (int) minions.values().stream().filter(VirtualMinion::start).count();
        log.info("Started {} minion(s)", started);
        return started;
    }

    public synchronized int stopAll() {
        int stopped = (int) minions.values().stream().filter(VirtualMinion::stop).count();
        log.info("Stopped {} minion(s)", stopped);
        return stopped;
    }

    public boolean start(String imei) {
        return get(imei).start();
    }

    public boolean stop(String imei) {
        return get(imei).stop();
    }

    public MinionStatus status(String imei) {
        return get(imei).status();
    }

    public List<MinionStatus> statuses() {
        return minions.values().stream().map(VirtualMinion::status).toList();
    }

    public Collection<VirtualMinion> getMinions() {
        return minions.values();
    }

    private VirtualMinion get(String imei) {
        VirtualMinion m = minions.get(imei);
        if (m == null) {
            throw new MinionNotFoundException(imei);
        }
        return m;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Application shutting down, stopping all minions");
        stopAll();
    }
}
