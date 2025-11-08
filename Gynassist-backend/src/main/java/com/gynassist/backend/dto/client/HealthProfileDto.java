package com.gynassist.backend.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for health profile summary.
 * Used for API responses without exposing full entity structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfileDto {
    private Long id;
    private Long userId;
    private Integer completionPercentage;
    private Boolean hasMedicalVitals;
    private Boolean hasMedicalHistory;
    private Boolean hasGynecologicalProfile;
}

