package com.gynassist.backend.service;

import com.gynassist.backend.dto.client.GynecologicalProfileDto;
import com.gynassist.backend.entity.User;
import com.gynassist.backend.entity.client.ClientHealthProfile;
import com.gynassist.backend.entity.client.GynecologicalProfile;
import com.gynassist.backend.entity.client.MenstruationCycle;
import com.gynassist.backend.repository.UserRepository;
import com.gynassist.backend.repository.client.ClientHealthProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GynecologicalProfileService {
    
    private final ClientHealthProfileRepository healthProfileRepository;
    private final UserRepository userRepository;
    
    public GynecologicalProfileDto getGynecologicalProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        ClientHealthProfile healthProfile = healthProfileRepository.findByUserId(user.getId())
                .orElse(null);
        
        if (healthProfile == null || healthProfile.getGynecologicalProfile() == null) {
            return GynecologicalProfileDto.builder().build();
        }
        
        return mapToDto(healthProfile.getGynecologicalProfile());
    }
    
    public GynecologicalProfileDto createOrUpdateGynecologicalProfile(String email, GynecologicalProfileDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        ClientHealthProfile healthProfile = healthProfileRepository.findByUserId(user.getId())
                .orElse(ClientHealthProfile.builder()
                        .user(user)
                        .profileCompletionPercentage(0)
                        .build());
        
        GynecologicalProfile gynProfile = healthProfile.getGynecologicalProfile();
        if (gynProfile == null) {
            gynProfile = GynecologicalProfile.builder()
                    .healthProfile(healthProfile)
                    .build();
            healthProfile.setGynecologicalProfile(gynProfile);
        }
        
        // Update fields
        updateGynecologicalProfileFields(gynProfile, dto);
        
        // Update completion percentage
        healthProfile.setProfileCompletionPercentage(calculateCompletionPercentage(healthProfile));
        
        healthProfile = healthProfileRepository.save(healthProfile);
        return mapToDto(healthProfile.getGynecologicalProfile());
    }
    
    public GynecologicalProfileDto.CycleEntryDto addCycleEntry(String email, GynecologicalProfileDto.CycleEntryDto cycleDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        ClientHealthProfile healthProfile = healthProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Health profile not found"));
        
        GynecologicalProfile gynProfile = healthProfile.getGynecologicalProfile();
        if (gynProfile == null) {
            throw new RuntimeException("Gynecological profile not found");
        }
        
        MenstruationCycle cycle = MenstruationCycle.builder()
                .gynecologicalProfile(gynProfile)
                .startDate(cycleDto.getStartDate())
                .endDate(cycleDto.getEndDate())
                .flowIntensity(cycleDto.getFlowIntensity() != null ? 
                    MenstruationCycle.FlowIntensity.valueOf(cycleDto.getFlowIntensity()) : null)
                .symptoms(cycleDto.getSymptoms() != null ? 
                    String.join(",", cycleDto.getSymptoms()) : null)
                .moodNotes(cycleDto.getMoodNotes())
                .painLevel(cycleDto.getPainLevel())
                .notes(cycleDto.getNotes())
                .isPredicted(cycleDto.getIsPredicted() != null ? cycleDto.getIsPredicted() : false)
                .build();
        
        // Calculate cycle length if end date is provided
        if (cycle.getStartDate() != null && cycle.getEndDate() != null) {
            cycle.setCycleLength((int) ChronoUnit.DAYS.between(cycle.getStartDate(), cycle.getEndDate()) + 1);
        }
        
        gynProfile.getCycles().add(cycle);
        healthProfileRepository.save(healthProfile);
        
        return mapCycleToDto(cycle);
    }
    
    public List<LocalDate> predictNextPeriods(String email, int monthsAhead) {
        GynecologicalProfileDto profile = getGynecologicalProfile(email);
        
        if (profile.getLastPeriodDate() == null || profile.getAverageCycleLength() == null) {
            return List.of();
        }
        
        List<LocalDate> predictions = new java.util.ArrayList<>();
        LocalDate nextPeriod = profile.getLastPeriodDate().plusDays(profile.getAverageCycleLength());
        
        for (int i = 0; i < monthsAhead; i++) {
            predictions.add(nextPeriod);
            nextPeriod = nextPeriod.plusDays(profile.getAverageCycleLength());
        }
        
        return predictions;
    }
    
    private void updateGynecologicalProfileFields(GynecologicalProfile profile, GynecologicalProfileDto dto) {
        if (dto.getAgeAtFirstPeriod() != null) {
            profile.setAgeAtFirstPeriod(dto.getAgeAtFirstPeriod());
        }
        if (dto.getAverageCycleLength() != null) {
            profile.setAverageCycleLength(dto.getAverageCycleLength());
        }
        if (dto.getLastPeriodDate() != null) {
            profile.setLastPeriodDate(dto.getLastPeriodDate());
        }
        if (dto.getCycleRegularity() != null) {
            profile.setCycleRegularity(dto.getCycleRegularity());
        }
        if (dto.getFlowIntensity() != null) {
            profile.setFlowIntensity(dto.getFlowIntensity());
        }
        if (dto.getPregnanciesCount() != null) {
            profile.setPregnanciesCount(dto.getPregnanciesCount());
        }
        if (dto.getLiveBirthsCount() != null) {
            profile.setLiveBirthsCount(dto.getLiveBirthsCount());
        }
        if (dto.getMiscarriagesCount() != null) {
            profile.setMiscarriagesCount(dto.getMiscarriagesCount());
        }
        if (dto.getContraceptionMethod() != null) {
            profile.setContraceptionMethod(dto.getContraceptionMethod());
        }
        if (dto.getFertilityGoal() != null) {
            profile.setFertilityGoal(dto.getFertilityGoal());
        }
        if (dto.getTryingToConcieveMonths() != null) {
            profile.setTryingToConcieveMonths(dto.getTryingToConcieveMonths());
        }
        if (dto.getSmokingStatus() != null) {
            profile.setSmokingStatus(dto.getSmokingStatus());
        }
        if (dto.getAlcoholConsumption() != null) {
            profile.setAlcoholConsumption(dto.getAlcoholConsumption());
        }
        if (dto.getExerciseFrequency() != null) {
            profile.setExerciseFrequency(dto.getExerciseFrequency());
        }
        if (dto.getStressLevel() != null) {
            profile.setStressLevel(dto.getStressLevel());
        }
    }
    
    private GynecologicalProfileDto mapToDto(GynecologicalProfile profile) {
        List<GynecologicalProfileDto.CycleEntryDto> recentCycles = profile.getCycles() != null ?
                profile.getCycles().stream()
                        .sorted((c1, c2) -> c2.getStartDate().compareTo(c1.getStartDate()))
                        .limit(6)
                        .map(this::mapCycleToDto)
                        .collect(Collectors.toList()) : List.of();
        
        return GynecologicalProfileDto.builder()
                .id(profile.getId())
                .ageAtFirstPeriod(profile.getAgeAtFirstPeriod())
                .averageCycleLength(profile.getAverageCycleLength())
                .lastPeriodDate(profile.getLastPeriodDate())
                .cycleRegularity(profile.getCycleRegularity())
                .flowIntensity(profile.getFlowIntensity())
                .pregnanciesCount(profile.getPregnanciesCount())
                .liveBirthsCount(profile.getLiveBirthsCount())
                .miscarriagesCount(profile.getMiscarriagesCount())
                .contraceptionMethod(profile.getContraceptionMethod())
                .fertilityGoal(profile.getFertilityGoal())
                .tryingToConcieveMonths(profile.getTryingToConcieveMonths())
                .smokingStatus(profile.getSmokingStatus())
                .alcoholConsumption(profile.getAlcoholConsumption())
                .exerciseFrequency(profile.getExerciseFrequency())
                .stressLevel(profile.getStressLevel())
                .recentCycles(recentCycles)
                .build();
    }
    
    private GynecologicalProfileDto.CycleEntryDto mapCycleToDto(MenstruationCycle cycle) {
        List<String> symptoms = cycle.getSymptoms() != null ? 
                Arrays.asList(cycle.getSymptoms().split(",")) : List.of();
        
        return GynecologicalProfileDto.CycleEntryDto.builder()
                .id(cycle.getId())
                .startDate(cycle.getStartDate())
                .endDate(cycle.getEndDate())
                .cycleLength(cycle.getCycleLength())
                .flowIntensity(cycle.getFlowIntensity() != null ? cycle.getFlowIntensity().name() : null)
                .symptoms(symptoms)
                .moodNotes(cycle.getMoodNotes())
                .painLevel(cycle.getPainLevel())
                .notes(cycle.getNotes())
                .isPredicted(cycle.getIsPredicted())
                .build();
    }
    
    private Integer calculateCompletionPercentage(ClientHealthProfile profile) {
        int totalFields = 15; // Total important fields across all profiles
        int completedFields = 0;
        
        // Basic profile fields
        if (profile.getEmergencyContactName() != null && !profile.getEmergencyContactName().trim().isEmpty()) {
            completedFields++;
        }
        if (profile.getEmergencyContactPhone() != null && !profile.getEmergencyContactPhone().trim().isEmpty()) {
            completedFields++;
        }
        
        // Gynecological profile fields
        GynecologicalProfile gynProfile = profile.getGynecologicalProfile();
        if (gynProfile != null) {
            if (gynProfile.getAgeAtFirstPeriod() != null) completedFields++;
            if (gynProfile.getAverageCycleLength() != null) completedFields++;
            if (gynProfile.getLastPeriodDate() != null) completedFields++;
            if (gynProfile.getCycleRegularity() != null) completedFields++;
            if (gynProfile.getFlowIntensity() != null) completedFields++;
            if (gynProfile.getContraceptionMethod() != null) completedFields++;
            if (gynProfile.getFertilityGoal() != null) completedFields++;
            if (gynProfile.getSmokingStatus() != null) completedFields++;
            if (gynProfile.getAlcoholConsumption() != null) completedFields++;
            if (gynProfile.getExerciseFrequency() != null) completedFields++;
            if (gynProfile.getStressLevel() != null) completedFields++;
        }
        
        return (int) Math.round((double) completedFields / totalFields * 100);
    }
}