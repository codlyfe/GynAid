package com.gynassist.backend.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfileDto {
    
    private Long id;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;
    private Integer profileCompletionPercentage;
    private MedicalVitalsDto vitals;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicalVitalsDto {
        private Double heightCm;
        private Double weightKg;
        private Integer bloodPressureSystolic;
        private Integer bloodPressureDiastolic;
        private String bloodType;
    }
}