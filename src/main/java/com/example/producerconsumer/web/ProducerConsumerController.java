package com.example.producerconsumer.web;

import com.example.producerconsumer.QueueVisualizer;
import com.example.producerconsumer.services.GuiRunnerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ProducerConsumerController {

    private final GuiRunnerService guiRunnerService;
    private final QueueVisualizer queueVisualizer;

    public ProducerConsumerController(GuiRunnerService guiRunnerService, QueueVisualizer queueVisualizer) {
        this.guiRunnerService = guiRunnerService;
        this.queueVisualizer = queueVisualizer;
    }

    public static class StartRequest {
        public int producers;
        public int consumers;
    }

    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestBody StartRequest req) {
        if (req.producers < 1 || req.consumers < req.producers) {
            return ResponseEntity.badRequest().body("Invalid parameters");
        }
        guiRunnerService.start(req.producers, req.consumers);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stop")
    public void stop() {
        guiRunnerService.stop();
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "running", guiRunnerService.isRunning(),
                "size", queueVisualizer.getCurrentSize(),
                "capacity", queueVisualizer.getCapacity()
        );
    }
}
