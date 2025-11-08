package com.gynassist.backend.entity.client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_vitals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalVitals {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_profile_id", nullable = false)
    private ClientHealthProfile healthProfile;
    
    @Column(name = "height_cm")
    private Double heightCm;
    
    @Column(name = "weight_kg")
    private Double weightKg;
    
    @Column(name = "blood_pressure_systolic")
    private Integer bloodPressureSystolic;
    
    @Column(name = "blood_pressure_diastolic")
    private Integer bloodPressureDiastolic;
    
    @Column(name = "blood_type")
    private String bloodType;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}