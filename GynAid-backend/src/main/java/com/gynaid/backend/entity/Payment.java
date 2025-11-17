package com.gynaid.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "UGX"; // Uganda Shillings by default

    @Column(name = "payment_method")
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private PaymentStatus status = PaymentStatus.INITIATED;

    @Column(name = "provider_share", precision = 10, scale = 2)
    private BigDecimal providerShare;

    @Column(name = "platform_fee", precision = 10, scale = 2)
    private BigDecimal platformFee;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "receipt_url", columnDefinition = "TEXT")
    private String receiptUrl;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "webhook_signature_verified")
    @Builder.Default
    private Boolean webhookSignatureVerified = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        INITIATED,    // Payment intent created
        SUCCEEDED,    // Payment completed successfully
        FAILED,       // Payment failed
        REFUNDED      // Payment refunded
    }

    // Helper methods for payment processing
    public void markSucceeded() {
        this.status = PaymentStatus.SUCCEEDED;
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }

    // Calculate provider share (typically 85% of total amount)
    public void calculateProviderShare(BigDecimal platformFeeAmount) {
        this.platformFee = platformFeeAmount;
        this.providerShare = this.amount.subtract(platformFeeAmount);
    }
}