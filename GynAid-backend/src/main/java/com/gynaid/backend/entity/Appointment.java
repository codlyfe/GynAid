package com.gynaid.backend.entity;

import com.gynaid.backend.entity.client.ClientHealthProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "provider_notes", columnDefinition = "TEXT")
    private String providerNotes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Consultation consultation;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AppointmentAuditTrail> auditTrail = new ArrayList<>();

    public enum AppointmentStatus {
        PENDING,      // Initial booking status
        APPROVED,     // Provider approved
        DECLINED,     // Provider declined
        CANCELLED,    // Cancelled by client or provider
        COMPLETED,    // Consultation completed
        NO_SHOW       // Client didn't show up
    }

    public enum PaymentStatus {
        UNPAID,       // Payment not yet made
        PAID,         // Payment completed
        FAILED,       // Payment failed
        REFUNDED      // Payment refunded
    }

    // Helper methods for state transitions
    public void approve() {
        if (this.status == AppointmentStatus.PENDING) {
            this.status = AppointmentStatus.APPROVED;
            addAuditEntry("APPROVED", "Appointment approved by provider");
        }
    }

    public void decline() {
        if (this.status == AppointmentStatus.PENDING) {
            this.status = AppointmentStatus.DECLINED;
            addAuditEntry("DECLINED", "Appointment declined by provider");
        }
    }

    public void cancel(String reason) {
        if (this.status == AppointmentStatus.PENDING || this.status == AppointmentStatus.APPROVED) {
            this.status = AppointmentStatus.CANCELLED;
            addAuditEntry("CANCELLED", "Appointment cancelled: " + reason);
        }
    }

    public void complete() {
        if (this.status == AppointmentStatus.APPROVED) {
            this.status = AppointmentStatus.COMPLETED;
            addAuditEntry("COMPLETED", "Consultation completed successfully");
        }
    }

    public void markNoShow() {
        if (this.status == AppointmentStatus.APPROVED) {
            this.status = AppointmentStatus.NO_SHOW;
            addAuditEntry("NO_SHOW", "Client did not appear for appointment");
        }
    }

    public void markAsPaid() {
        this.paymentStatus = PaymentStatus.PAID;
        addAuditEntry("PAYMENT_RECEIVED", "Payment status updated to PAID");
    }

    public void refund() {
        this.paymentStatus = PaymentStatus.REFUNDED;
        addAuditEntry("REFUNDED", "Payment refunded");
    }

    public void addAuditEntry(String action, String details) {
        // This will be populated when we create the AuditTrail entity
    }
}