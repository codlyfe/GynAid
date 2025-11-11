package com.gynaid.backend.service;

import com.gynaid.backend.dto.client.HealthProfileDto;
import com.gynaid.backend.entity.User;
import com.gynaid.backend.entity.client.ClientHealthProfile;
import com.gynaid.backend.entity.client.MedicalVitals;
import com.gynaid.backend.repository.UserRepository;
import com.gynaid.backend.repository.client.ClientHealthProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientProfileService {
    
    private final ClientHealthProfileRepository healthProfileRepository;
    private final UserRepository userRepository;
    
    public HealthProfileDto getHealthProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        ClientHealthProfile profile = healthProfileRepository.findByUserId(user.getId())
                .orElse(null);
        
        if (profile == null) {
            return HealthProfileDto.builder()
                    .profileCompletionPercentage(0)
                    .build();
        }
        
        return mapToDto(profile);
    }
    
    public HealthProfileDto createOrUpdateHealthProfile(String email, HealthProfileDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        ClientHealthProfile profile = healthProfileRepository.findByUserId(user.getId())
                .orElse(ClientHealthProfile.builder()
                        .user(user)
                        .profileCompletionPercentage(0)
                        .build());
        
        // Update profile fields
        if (dto.getEmergencyContactName() != null) {
            profile.setEmergencyContactName(dto.getEmergencyContactName());
        }
        if (dto.getEmergencyContactPhone() != null) {
            profile.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        }
        if (dto.getEmergencyContactRelationship() != null) {
            profile.setEmergencyContactRelationship(dto.getEmergencyContactRelationship());
        }
        
        // Calculate completion percentage
        profile.setProfileCompletionPercentage(calculateCompletionPercentage(profile));
        
        profile = healthProfileRepository.save(profile);
        return mapToDto(profile);
    }
    
    public Integer getProfileCompletionPercentage(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return healthProfileRepository.findByUserId(user.getId())
                .map(ClientHealthProfile::getProfileCompletionPercentage)
                .orElse(0);
    }
    
    private HealthProfileDto mapToDto(ClientHealthProfile profile) {
        return HealthProfileDto.builder()
                .id(profile.getId())
                .emergencyContactName(profile.getEmergencyContactName())
                .emergencyContactPhone(profile.getEmergencyContactPhone())
                .emergencyContactRelationship(profile.getEmergencyContactRelationship())
                .profileCompletionPercentage(profile.getProfileCompletionPercentage())
                .build();
    }
    
    private Integer calculateCompletionPercentage(ClientHealthProfile profile) {
        int totalFields = 3; // emergency contact fields
        int completedFields = 0;
        
        if (profile.getEmergencyContactName() != null && !profile.getEmergencyContactName().trim().isEmpty()) {
            completedFields++;
        }
        if (profile.getEmergencyContactPhone() != null && !profile.getEmergencyContactPhone().trim().isEmpty()) {
            completedFields++;
        }
        if (profile.getEmergencyContactRelationship() != null && !profile.getEmergencyContactRelationship().trim().isEmpty()) {
            completedFields++;
        }
        
        return (int) Math.round((double) completedFields / totalFields * 100);
    }
}
