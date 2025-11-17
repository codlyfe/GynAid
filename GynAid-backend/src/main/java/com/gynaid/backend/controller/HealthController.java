package com.gynaid.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Slf4j
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("application", "GynAid Backend");
        response.put("version", "1.0.0");
        response.put("environment", System.getProperty("spring.profiles.active", "development"));
        
        log.debug("Health check requested");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "GynAid Backend API");
        response.put("version", "1.0.0");
        response.put("description", "Healthcare Platform Backend API");
        response.put("java", System.getProperty("java.version"));
        response.put("os", System.getProperty("os.name"));
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> response = new HashMap<>();
        response.put("heap.memory.used", runtime.totalMemory() - runtime.freeMemory());
        response.put("heap.memory.max", runtime.maxMemory());
        response.put("heap.memory.free", runtime.freeMemory());
        response.put("heap.memory.total", runtime.totalMemory());
        response.put("available.processors", runtime.availableProcessors());
        response.put("threads.count", Thread.getAllStackTraces().size());
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "READY");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("checks", Map.of(
            "database", "UP", // You can add actual DB health check here
            "memory", getMemoryStatus(),
            "disk", getDiskStatus()
        ));
        
        return ResponseEntity.ok(response);
    }

    private String getMemoryStatus() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double usagePercent = (double) usedMemory / maxMemory * 100;
        
        if (usagePercent > 90) {
            return "WARNING";
        } else if (usagePercent > 80) {
            return "DEGRADED";
        } else {
            return "UP";
        }
    }

    private String getDiskStatus() {
        // Basic disk status check - you can enhance this
        return "UP";
    }
}