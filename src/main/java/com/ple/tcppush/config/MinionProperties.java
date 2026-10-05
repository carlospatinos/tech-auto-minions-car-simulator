package com.ple.tcppush.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Validated
@ConfigurationProperties(prefix = "minions")
@Data 
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

    @Data 
    public static class Defaults {
        @NotBlank
        private String host = "localhost";
        @Min(1)
        private int port = 9000;
        @Min(0)
        private long waitTimeMs = 1000;
        private List<String> messages = new ArrayList<>();
    }

    /** Per-minion overrides; null fields fall back to {@link Defaults}. */
    @Data 
    public static class Override {
        private String imei;
        private String host;
        private Integer port;
        private Long waitTimeMs;
        private List<String> messages;
    }
}
