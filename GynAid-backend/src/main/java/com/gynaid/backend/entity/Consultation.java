package com.gynaid.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private HealthcareProvider provider;

    @Column(nullable = false)
    private LocalDateTime scheduledDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsultationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsultationStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal appFee; // 10% of consultation fee

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String paymentTransactionId;
    private LocalDateTime paymentDateTime;

    @Column(columnDefinition = "TEXT")
    private String clientNotes;

    @Column(columnDefinition = "TEXT")
    private String providerNotes;

    private String meetingLink;
    private String meetingId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ConsultationType {
        VIDEO_CALL,
        PHONE_CALL,
        IN_PERSON,
        CHAT_CONSULTATION
    }

    public enum ConsultationStatus {
        PENDING_PAYMENT,
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        NO_SHOW
    }

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REFUNDED
    }

    public enum PaymentMethod {
        MTN_MOBILE_MONEY,
        AIRTEL_MONEY,
        BANK_TRANSFER,
        CREDIT_CARD,
        CASH
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ConsultationStatus.PENDING_PAYMENT;
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
