package com.example.tcppush.web;

import com.example.tcppush.minion.MinionManager;
import com.example.tcppush.minion.MinionNotFoundException;
import com.example.tcppush.minion.MinionStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/minions")
public class MinionController {

    private final MinionManager manager;

    public MinionController(MinionManager manager) {
        this.manager = manager;
    }

    @GetMapping
    public List<MinionStatus> list() {
        return manager.statuses();
    }

    @GetMapping("/{imei}")
    public MinionStatus get(@PathVariable String imei) {
        return manager.status(imei);
    }

    @PostMapping("/start")
    public Map<String, Integer> startAll() {
        return Map.of("started", manager.startAll());
    }

    @PostMapping("/stop")
    public Map<String, Integer> stopAll() {
        return Map.of("stopped", manager.stopAll());
    }

    @PostMapping("/{imei}/start")
    public Map<String, Object> start(@PathVariable String imei) {
        return Map.of("imei", imei, "started", manager.start(imei));
    }

    @PostMapping("/{imei}/stop")
    public Map<String, Object> stop(@PathVariable String imei) {
        return Map.of("imei", imei, "stopped", manager.stop(imei));
    }

    @ExceptionHandler(MinionNotFoundException.class)
    public ProblemDetail notFound(MinionNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
