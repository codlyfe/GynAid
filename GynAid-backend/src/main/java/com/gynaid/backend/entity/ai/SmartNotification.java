package com.gynaid.backend.entity.ai;

import com.gynaid.backend.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "smart_notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    @Builder.Default
    private Priority priority = Priority.MEDIUM;
    
    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "is_sent")
    @Builder.Default
    private Boolean isSent = false;
    
    public enum NotificationType {
        PERIOD_REMINDER, FERTILITY_WINDOW, MEDICATION_REMINDER,
        APPOINTMENT_REMINDER, HEALTH_TIP, EMERGENCY_ALERT, MOH_UPDATE
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
}
