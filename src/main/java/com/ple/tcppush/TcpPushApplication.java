package com.ple.tcppush;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TcpPushApplication {

    public static void main(String[] args) {
        SpringApplication.run(TcpPushApplication.class, args);
    }
}
