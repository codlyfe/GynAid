package com.gynaid.backend.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for medical vitals.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalVitalsDto {
    private Long id;
    private Double heightCm;
    private Double weightKg;
    private String bloodGroup;
    private Integer systolicBp;
    private Integer diastolicBp;
    private Double bmi; // Calculated field
}


