package com.gynaid.backend.entity.provider;

import com.gynaid.backend.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

/**
 * Practice information for healthcare providers.
 * Includes clinic/hospital details, operating hours, and services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "provider_practice_info")
public class ProviderPracticeInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false, unique = true)
    private User provider;

    @Column(name = "practice_name")
    private String practiceName; // Clinic/Hospital name

    @Column(name = "practice_type")
    @Enumerated(EnumType.STRING)
    private PracticeType practiceType;

    @Column(name = "physical_address", columnDefinition = "TEXT")
    private String physicalAddress;

    // Geographic coordinates for mapping
    @Column(name = "practice_location", columnDefinition = "geometry(Point,4326)")
    private Point practiceLocation;

    @Column(name = "city")
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "country")
    @Builder.Default
    private String country = "Uganda";

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    // Operating hours (stored as JSON for flexibility)
    @Column(name = "operating_hours", columnDefinition = "TEXT")
    private String operatingHours; // JSON: {"monday": {"open": "08:00", "close": "17:00"}, ...}

    // Services offered (stored as JSON array)
    @Column(name = "services_offered", columnDefinition = "TEXT")
    private String servicesOffered; // JSON array

    // Consultation fees
    @Column(name = "virtual_consultation_fee")
    private Double virtualConsultationFee; // In Ugandan Shillings

    @Column(name = "in_person_consultation_fee")
    private Double inPersonConsultationFee;

    @Column(name = "home_visit_fee")
    private Double homeVisitFee;

    // Payment methods accepted
    @Column(name = "payment_methods", columnDefinition = "TEXT")
    private String paymentMethods; // JSON array: ["mobile_money", "stripe", "cash"]

    // Mobile Money account details (encrypted)
    @Column(name = "mobile_money_number")
    private String mobileMoneyNumber;

    @Column(name = "mobile_money_provider")
    @Enumerated(EnumType.STRING)
    private MobileMoneyProvider mobileMoneyProvider;

    // Stripe account (store Stripe account ID, not full details)
    @Column(name = "stripe_account_id")
    private String stripeAccountId;

    @Column(name = "stripe_account_verified")
    @Builder.Default
    private Boolean stripeAccountVerified = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PracticeType {
        PRIVATE_CLINIC,
        HOSPITAL,
        HEALTH_CENTER,
        MOBILE_CLINIC,
        HOME_BASED,
        OTHER
    }

    public enum MobileMoneyProvider {
        MTN_MOBILE_MONEY,
        AIRTEL_MONEY,
        OTHER
    }
}


