package com.gynassist.backend.service;

import com.gynassist.backend.entity.User;
import com.gynassist.backend.entity.client.*;
import com.gynassist.backend.repository.client.ClientHealthProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientHealthProfileService {
    
    private final ClientHealthProfileRepository healthProfileRepository;
    
    public Optional<ClientHealthProfile> getHealthProfile(Long userId) {
        return healthProfileRepository.findByUserId(userId);
    }
    
    public ClientHealthProfile createOrUpdateHealthProfile(User user, ClientHealthProfile profile) {
        Optional<ClientHealthProfile> existing = healthProfileRepository.findByUserId(user.getId());
        
        if (existing.isPresent()) {
            ClientHealthProfile existingProfile = existing.get();
            updateProfileFields(existingProfile, profile);
            return healthProfileRepository.save(existingProfile);
        } else {
            profile.setUser(user);
            return healthProfileRepository.save(profile);
        }
    }
    
    public MedicalVitals updateMedicalVitals(Long userId, MedicalVitals vitals) {
        ClientHealthProfile profile = getOrCreateHealthProfile(userId);
        
        if (profile.getMedicalVitals() != null) {
            MedicalVitals existing = profile.getMedicalVitals();
            updateVitalsFields(existing, vitals);
            return existing;
        } else {
            vitals.setHealthProfile(profile);
            profile.setMedicalVitals(vitals);
            return vitals;
        }
    }
    
    public MedicalHistory updateMedicalHistory(Long userId, MedicalHistory history) {
        ClientHealthProfile profile = getOrCreateHealthProfile(userId);
        
        if (profile.getMedicalHistory() != null) {
            MedicalHistory existing = profile.getMedicalHistory();
            updateHistoryFields(existing, history);
            return existing;
        } else {
            history.setHealthProfile(profile);
            profile.setMedicalHistory(history);
            return history;
        }
    }
    
    private ClientHealthProfile getOrCreateHealthProfile(Long userId) {
        return healthProfileRepository.findByUserId(userId)
            .orElseGet(() -> {
                ClientHealthProfile newProfile = ClientHealthProfile.builder()
                    .user(User.builder().id(userId).build())
                    .build();
                return healthProfileRepository.save(newProfile);
            });
    }
    
    private void updateProfileFields(ClientHealthProfile existing, ClientHealthProfile updated) {
        if (updated.getEmergencyContactName() != null) {
            existing.setEmergencyContactName(updated.getEmergencyContactName());
        }
        if (updated.getEmergencyContactPhone() != null) {
            existing.setEmergencyContactPhone(updated.getEmergencyContactPhone());
        }
        if (updated.getEmergencyContactRelationship() != null) {
            existing.setEmergencyContactRelationship(updated.getEmergencyContactRelationship());
        }
    }
    
    private void updateVitalsFields(MedicalVitals existing, MedicalVitals updated) {
        if (updated.getHeightCm() != null) existing.setHeightCm(updated.getHeightCm());
        if (updated.getWeightKg() != null) existing.setWeightKg(updated.getWeightKg());
        if (updated.getBloodPressureSystolic() != null) existing.setBloodPressureSystolic(updated.getBloodPressureSystolic());
        if (updated.getBloodPressureDiastolic() != null) existing.setBloodPressureDiastolic(updated.getBloodPressureDiastolic());
        if (updated.getBloodType() != null) existing.setBloodType(updated.getBloodType());
    }
    
    private void updateHistoryFields(MedicalHistory existing, MedicalHistory updated) {
        if (updated.getChronicConditions() != null) existing.setChronicConditions(updated.getChronicConditions());
        if (updated.getAllergies() != null) existing.setAllergies(updated.getAllergies());
        if (updated.getCurrentMedications() != null) existing.setCurrentMedications(updated.getCurrentMedications());
        if (updated.getFamilyHistory() != null) existing.setFamilyHistory(updated.getFamilyHistory());
        if (updated.getHivStatus() != null) existing.setHivStatus(updated.getHivStatus());
        if (updated.getDisclosurePreference() != null) existing.setDisclosurePreference(updated.getDisclosurePreference());
    }
}