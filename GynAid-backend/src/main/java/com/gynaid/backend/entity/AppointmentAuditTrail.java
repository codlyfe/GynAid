package com.gynaid.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Audit trail entity for tracking all appointment actions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointment_audit_trail")
public class AppointmentAuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "action", nullable = false, length = 50)
    private String action; // CREATED, APPROVED, DECLINED, CANCELLED, RESCHEDULED, COMPLETED, NO_SHOW, PAYMENT_RECEIVED, REFUNDED

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "new_status")
    private String newStatus;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    public enum AuditAction {
        CREATED("Appointment created"),
        APPROVED("Appointment approved"),
        DECLINED("Appointment declined"),
        CANCELLED("Appointment cancelled"),
        RESCHEDULED("Appointment rescheduled"),
        COMPLETED("Consultation completed"),
        NO_SHOW("Client marked as no-show"),
        PAYMENT_RECEIVED("Payment received"),
        REFUNDED("Payment refunded"),
        ADMIN_OVERRIDE("Admin override performed"),
        NOTES_ADDED("Provider notes added"),
        CLIENT_NOTES("Client notes added"),
        STATUS_CHANGED("Status changed manually");

        private final String description;

        AuditAction(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}