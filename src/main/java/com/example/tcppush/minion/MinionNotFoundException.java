package com.example.tcppush.minion;

public class MinionNotFoundException extends RuntimeException {

    public MinionNotFoundException(String imei) {
        super("Minion not found: " + imei);
    }
}
