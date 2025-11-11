package com.gynaid.backend.dto.client;

import com.gynaid.backend.entity.client.GynecologicalProfile.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GynecologicalProfileDto {
    
    private Long id;
    private Integer ageAtFirstPeriod;
    private Integer averageCycleLength;
    private LocalDate lastPeriodDate;
    private CycleRegularity cycleRegularity;
    private FlowIntensity flowIntensity;
    private Integer pregnanciesCount;
    private Integer liveBirthsCount;
    private Integer miscarriagesCount;
    private ContraceptionMethod contraceptionMethod;
    private FertilityGoal fertilityGoal;
    private Integer tryingToConcieveMonths;
    private SmokingStatus smokingStatus;
    private AlcoholConsumption alcoholConsumption;
    private ExerciseFrequency exerciseFrequency;
    private Integer stressLevel;
    private List<CycleEntryDto> recentCycles;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CycleEntryDto {
        private Long id;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer cycleLength;
        private String flowIntensity;
        private List<String> symptoms;
        private String moodNotes;
        private Integer painLevel;
        private String notes;
        private Boolean isPredicted;
    }
}
