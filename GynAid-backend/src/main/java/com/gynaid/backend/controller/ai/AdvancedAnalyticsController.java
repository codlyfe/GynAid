package com.gynaid.backend.controller.ai;

import com.gynaid.backend.entity.ai.HealthTrend;
import com.gynaid.backend.service.ai.AdvancedAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdvancedAnalyticsController {
    
    private final AdvancedAnalyticsService advancedAnalyticsService;
    
    @GetMapping("/health-trends/{userId}")
    public ResponseEntity<List<HealthTrend>> getHealthTrends(@PathVariable Long userId) {
        List<HealthTrend> trends = advancedAnalyticsService.analyzeHealthTrends(userId);
        return ResponseEntity.ok(trends);
    }
    
    @GetMapping("/health-report/{userId}")
    public ResponseEntity<Map<String, Object>> getHealthReport(@PathVariable Long userId) {
        Map<String, Object> report = advancedAnalyticsService.generateHealthReport(userId);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/risk-assessment/{userId}")
    public ResponseEntity<Map<String, Object>> getRiskAssessment(@PathVariable Long userId) {
        Map<String, Object> assessment = advancedAnalyticsService.calculateRiskAssessment(userId);
        return ResponseEntity.ok(assessment);
    }
}
