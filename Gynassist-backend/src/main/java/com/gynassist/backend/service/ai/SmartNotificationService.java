package com.gynassist.backend.service.ai;

import com.gynassist.backend.entity.User;
import com.gynassist.backend.entity.ai.SmartNotification;
import com.gynassist.backend.entity.client.GynecologicalProfile;
import com.gynassist.backend.repository.UserRepository;
import com.gynassist.backend.repository.client.ClientHealthProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmartNotificationService {
    
    private final UserRepository userRepository;
    private final ClientHealthProfileRepository healthProfileRepository;
    private final PredictiveAnalyticsService predictiveAnalyticsService;
    
    public List<SmartNotification> generateSmartNotifications(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<SmartNotification> notifications = new ArrayList<>();
        GynecologicalProfile profile = getGynecologicalProfile(userId);
        
        // Generate period reminders enhancing existing prediction logic
        notifications.addAll(generatePeriodReminders(user, profile));
        
        // Generate fertility window notifications
        notifications.addAll(generateFertilityNotifications(user, profile));
        
        // Generate health tips based on user data
        notifications.addAll(generateHealthTips(user, profile));
        
        return notifications;
    }
    
    public SmartNotification createEmergencyAlert(Long userId, String alertMessage) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return SmartNotification.builder()
            .user(user)
            .notificationType(SmartNotification.NotificationType.EMERGENCY_ALERT)
            .title("Health Emergency Alert")
            .message(alertMessage)
            .priority(SmartNotification.Priority.URGENT)
            .scheduledFor(LocalDateTime.now())
            .build();
    }
    
    public List<SmartNotification> getPendingNotifications(Long userId) {
        // Get notifications scheduled for now or past that haven't been sent
        return List.of(); // Would implement with actual repository
    }
    
    private List<SmartNotification> generatePeriodReminders(User user, GynecologicalProfile profile) {
        List<SmartNotification> notifications = new ArrayList<>();
        
        try {
            LocalDate nextPeriod = predictiveAnalyticsService.predictNextPeriod(user.getId());
            if (nextPeriod != null) {
                // 3-day advance reminder
                notifications.add(SmartNotification.builder()
                    .user(user)
                    .notificationType(SmartNotification.NotificationType.PERIOD_REMINDER)
                    .title("Period Reminder")
                    .message("Your period is predicted to start in 3 days (" + nextPeriod + "). " +
                           "Consider preparing supplies and tracking any pre-menstrual symptoms.")
                    .priority(SmartNotification.Priority.MEDIUM)
                    .scheduledFor(nextPeriod.minusDays(3).atTime(9, 0))
                    .build());
                
                // Day-of reminder
                notifications.add(SmartNotification.builder()
                    .user(user)
                    .notificationType(SmartNotification.NotificationType.PERIOD_REMINDER)
                    .title("Period Day")
                    .message("Your period is predicted to start today. Don't forget to log when it begins for accurate tracking.")
                    .priority(SmartNotification.Priority.HIGH)
                    .scheduledFor(nextPeriod.atTime(8, 0))
                    .build());
            }
        } catch (Exception e) {
            log.warn("Could not generate period reminders for user {}: {}", user.getId(), e.getMessage());
        }
        
        return notifications;
    }
    
    private List<SmartNotification> generateFertilityNotifications(User user, GynecologicalProfile profile) {
        List<SmartNotification> notifications = new ArrayList<>();
        
        if (profile != null && profile.getFertilityGoal() == GynecologicalProfile.FertilityGoal.TRYING_TO_CONCEIVE) {
            try {
                LocalDate nextPeriod = predictiveAnalyticsService.predictNextPeriod(user.getId());
                if (nextPeriod != null) {
                    LocalDate fertileStart = nextPeriod.minusDays(16); // Approximate fertile window start
                    
                    notifications.add(SmartNotification.builder()
                        .user(user)
                        .notificationType(SmartNotification.NotificationType.FERTILITY_WINDOW)
                        .title("Fertility Window Starting")
                        .message("Your fertile window is beginning. This is an optimal time for conception. " +
                               "Track ovulation signs and maintain healthy habits.")
                        .priority(SmartNotification.Priority.HIGH)
                        .scheduledFor(fertileStart.atTime(7, 0))
                        .build());
                }
            } catch (Exception e) {
                log.warn("Could not generate fertility notifications for user {}: {}", user.getId(), e.getMessage());
            }
        }
        
        return notifications;
    }
    
    private List<SmartNotification> generateHealthTips(User user, GynecologicalProfile profile) {
        List<SmartNotification> notifications = new ArrayList<>();
        
        // Generate personalized health tips based on profile data
        if (profile != null) {
            // Stress management tip for high stress users
            if (profile.getStressLevel() != null && profile.getStressLevel() > 7) {
                notifications.add(SmartNotification.builder()
                    .user(user)
                    .notificationType(SmartNotification.NotificationType.HEALTH_TIP)
                    .title("Stress Management Tip")
                    .message("High stress levels can affect your cycle. Try 10 minutes of deep breathing, " +
                           "meditation, or gentle exercise today.")
                    .priority(SmartNotification.Priority.LOW)
                    .scheduledFor(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                    .build());
            }
            
            // Exercise encouragement for inactive users
            if (profile.getExerciseFrequency() == GynecologicalProfile.ExerciseFrequency.NEVER) {
                notifications.add(SmartNotification.builder()
                    .user(user)
                    .notificationType(SmartNotification.NotificationType.HEALTH_TIP)
                    .title("Exercise Tip")
                    .message("Regular exercise can help regulate your cycle and reduce menstrual symptoms. " +
                           "Start with a 15-minute walk today!")
                    .priority(SmartNotification.Priority.LOW)
                    .scheduledFor(LocalDateTime.now().plusDays(2).withHour(16).withMinute(0))
                    .build());
            }
        }
        
        return notifications;
    }
    
    private GynecologicalProfile getGynecologicalProfile(Long userId) {
        return healthProfileRepository.findByUserId(userId)
            .map(hp -> hp.getGynecologicalProfile())
            .orElse(null);
    }
}