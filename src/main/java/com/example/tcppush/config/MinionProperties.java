package com.example.tcppush.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "minions")
public class MinionProperties {

    @Min(0)
    private int count = 1;
    private boolean autoStart = true;
    @NotBlank
    private String imeiPrefix = "35693803";
    private boolean loop = false;
    @Min(0)
    private int connectTimeoutMs = 5000;
    @Valid
    @NotNull
    private Defaults defaults = new Defaults();
    private List<Override> instances = new ArrayList<>();

    public static class Defaults {
        @NotBlank
        private String host = "localhost";
        @Min(1)
        private int port = 9000;
        @Min(0)
        private long waitTimeMs = 1000;
        private List<String> messages = new ArrayList<>();

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public long getWaitTimeMs() { return waitTimeMs; }
        public void setWaitTimeMs(long waitTimeMs) { this.waitTimeMs = waitTimeMs; }
        public List<String> getMessages() { return messages; }
        public void setMessages(List<String> messages) { this.messages = messages; }
    }

    /** Per-minion overrides; null fields fall back to {@link Defaults}. */
    public static class Override {
        private String imei;
        private String host;
        private Integer port;
        private Long waitTimeMs;
        private List<String> messages;

        public String getImei() { return imei; }
        public void setImei(String imei) { this.imei = imei; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public Integer getPort() { return port; }
        public void setPort(Integer port) { this.port = port; }
        public Long getWaitTimeMs() { return waitTimeMs; }
        public void setWaitTimeMs(Long waitTimeMs) { this.waitTimeMs = waitTimeMs; }
        public List<String> getMessages() { return messages; }
        public void setMessages(List<String> messages) { this.messages = messages; }
    }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public boolean isAutoStart() { return autoStart; }
    public void setAutoStart(boolean autoStart) { this.autoStart = autoStart; }
    public String getImeiPrefix() { return imeiPrefix; }
    public void setImeiPrefix(String imeiPrefix) { this.imeiPrefix = imeiPrefix; }
    public boolean isLoop() { return loop; }
    public void setLoop(boolean loop) { this.loop = loop; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public Defaults getDefaults() { return defaults; }
    public void setDefaults(Defaults defaults) { this.defaults = defaults; }
    public List<Override> getInstances() { return instances; }
    public void setInstances(List<Override> instances) { this.instances = instances; }
}
