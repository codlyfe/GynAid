package com.gynassist.backend.controller.ai;

import com.gynassist.backend.entity.ai.SmartNotification;
import com.gynassist.backend.service.ai.SmartNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {
    
    private final SmartNotificationService smartNotificationService;
    
    @GetMapping("/generate/{userId}")
    public ResponseEntity<List<SmartNotification>> generateNotifications(@PathVariable Long userId) {
        List<SmartNotification> notifications = smartNotificationService.generateSmartNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/pending/{userId}")
    public ResponseEntity<List<SmartNotification>> getPendingNotifications(@PathVariable Long userId) {
        List<SmartNotification> pending = smartNotificationService.getPendingNotifications(userId);
        return ResponseEntity.ok(pending);
    }
    
    @PostMapping("/emergency-alert")
    public ResponseEntity<SmartNotification> createEmergencyAlert(
            @RequestParam Long userId,
            @RequestBody Map<String, String> request) {
        
        String alertMessage = request.get("message");
        if (alertMessage == null || alertMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        SmartNotification alert = smartNotificationService.createEmergencyAlert(userId, alertMessage);
        return ResponseEntity.ok(alert);
    }
}