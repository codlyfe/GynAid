package com.gynassist.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "provider_subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "provider_id")
    private HealthcareProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyFee;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPaid;

    private LocalDateTime lastPaymentDate;
    private LocalDateTime nextBillingDate;

    // Commercial features
    private Integer priorityRanking; // Higher number = higher priority in search
    private Boolean featuredListing;
    private Boolean premiumBadge;
    private Integer maxPhotos;
    private Boolean videoConsultationEnabled;
    private Boolean emergencyCallsEnabled;
    private Boolean analyticsAccess;
    private Boolean customBranding;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum SubscriptionPlan {
        BASIC(50000, 1, false, false, 3, false, false, false, false),
        PROFESSIONAL(150000, 3, true, true, 10, true, false, true, false),
        PREMIUM(300000, 5, true, true, 25, true, true, true, true),
        ENTERPRISE(500000, 10, true, true, 50, true, true, true, true);

        private final BigDecimal monthlyFee;
        private final Integer priorityRanking;
        private final Boolean featuredListing;
        private final Boolean premiumBadge;
        private final Integer maxPhotos;
        private final Boolean videoConsultationEnabled;
        private final Boolean emergencyCallsEnabled;
        private final Boolean analyticsAccess;
        private final Boolean customBranding;

        SubscriptionPlan(double monthlyFee, Integer priorityRanking, Boolean featuredListing, 
                        Boolean premiumBadge, Integer maxPhotos, Boolean videoConsultationEnabled,
                        Boolean emergencyCallsEnabled, Boolean analyticsAccess, Boolean customBranding) {
            this.monthlyFee = BigDecimal.valueOf(monthlyFee);
            this.priorityRanking = priorityRanking;
            this.featuredListing = featuredListing;
            this.premiumBadge = premiumBadge;
            this.maxPhotos = maxPhotos;
            this.videoConsultationEnabled = videoConsultationEnabled;
            this.emergencyCallsEnabled = emergencyCallsEnabled;
            this.analyticsAccess = analyticsAccess;
            this.customBranding = customBranding;
        }

        public BigDecimal getMonthlyFee() { return monthlyFee; }
        public Integer getPriorityRanking() { return priorityRanking; }
        public Boolean getFeaturedListing() { return featuredListing; }
        public Boolean getPremiumBadge() { return premiumBadge; }
        public Integer getMaxPhotos() { return maxPhotos; }
        public Boolean getVideoConsultationEnabled() { return videoConsultationEnabled; }
        public Boolean getEmergencyCallsEnabled() { return emergencyCallsEnabled; }
        public Boolean getAnalyticsAccess() { return analyticsAccess; }
        public Boolean getCustomBranding() { return customBranding; }
    }

    public enum SubscriptionStatus {
        ACTIVE,
        EXPIRED,
        SUSPENDED,
        CANCELLED,
        PENDING_PAYMENT
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = SubscriptionStatus.PENDING_PAYMENT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}