package com.gynassist.backend.entity.client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Medical vitals tracking for clients.
 * All fields nullable for gradual profile completion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "medical_vitals")
public class MedicalVitals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_profile_id", nullable = false, unique = true)
    private ClientHealthProfile healthProfile;

    // Basic vitals
    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "blood_group")
    private String bloodGroup; // A+, A-, B+, B-, AB+, AB-, O+, O-

    // Blood pressure tracking (stored as JSON or separate table for history)
    @Column(name = "systolic_bp")
    private Integer systolicBp;

    @Column(name = "diastolic_bp")
    private Integer diastolicBp;

    @Column(name = "last_bp_reading_date")
    private LocalDateTime lastBpReadingDate;

    // BMI (calculated field, but stored for quick access)
    @Column(name = "bmi")
    private Double bmi;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Calculates BMI if height and weight are available.
     */
    @PreUpdate
    @PrePersist
    public void calculateBMI() {
        if (heightCm != null && weightKg != null && heightCm > 0) {
            double heightInMeters = heightCm / 100.0;
            this.bmi = weightKg / (heightInMeters * heightInMeters);
        }
    }
}

