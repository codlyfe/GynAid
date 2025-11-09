package com.gynassist.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "healthcare_providers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthcareProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GeographicScope scope;

    @ElementCollection(targetClass = Specialization.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "provider_specializations")
    private List<Specialization> specializations;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

    private String address;
    private String district;
    private String region;
    private String country;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String website;
    private String licenseNumber;
    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availabilityStatus;

    private Double rating;
    private Integer reviewCount;

    @Column(name = "consultation_fee")
    private Double consultationFee;

    @ElementCollection
    @CollectionTable(name = "provider_languages")
    private List<String> languages;

    @ElementCollection
    @CollectionTable(name = "provider_services")
    private List<String> services;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActiveAt;

    public enum ProviderType {
        INDIVIDUAL_DOCTOR,
        HOSPITAL,
        CLINIC,
        HEALTH_CENTER,
        SPECIALIST_CENTER,
        TELEMEDICINE_PROVIDER
    }

    public enum Specialization {
        INFERTILITY_SUPPORT,
        ENDOMETRIOSIS_CARE,
        CYCLE_COMPLICATIONS,
        REPRODUCTIVE_INFECTIONS,
        GENERAL_GYNECOLOGY,
        OBSTETRICS,
        REPRODUCTIVE_ENDOCRINOLOGY,
        MATERNAL_FETAL_MEDICINE,
        GYNECOLOGIC_ONCOLOGY,
        FAMILY_PLANNING,
        ADOLESCENT_GYNECOLOGY
    }

    public enum GeographicScope {
        UGANDA,
        EAST_AFRICA,
        AFRICA,
        GLOBAL
    }

    public enum VerificationStatus {
        PENDING,
        VERIFIED,
        REJECTED,
        SUSPENDED
    }

    public enum AvailabilityStatus {
        AVAILABLE,
        BUSY,
        OFFLINE,
        ON_LEAVE
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (verificationStatus == null) {
            verificationStatus = VerificationStatus.PENDING;
        }
        if (availabilityStatus == null) {
            availabilityStatus = AvailabilityStatus.OFFLINE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}