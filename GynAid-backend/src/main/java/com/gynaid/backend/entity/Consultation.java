package com.gynaid.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "consultations")
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(name = "scheduled_date_time")
    private LocalDateTime scheduledDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ConsultationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private ConsultationStatus status = ConsultationStatus.PENDING_PAYMENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Column(name = "app_fee", precision = 10, scale = 2)
    private BigDecimal appFee;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_transaction_id")
    private String paymentTransactionId;

    @Column(name = "payment_date_time")
    private LocalDateTime paymentDateTime;

    @Column(name = "modality")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ConsultationModality modality = ConsultationModality.VIDEO;

    @Column(name = "room_id", unique = true)
    private String roomId;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "provider_notes", columnDefinition = "TEXT")
    private String providerNotes;

    @Column(name = "client_notes", columnDefinition = "TEXT")
    private String clientNotes;

    @ElementCollection
    @CollectionTable(name = "consultation_attachments",
                    joinColumns = @JoinColumn(name = "consultation_id"))
    @Column(name = "attachment_url")
    @Builder.Default
    private List<String> attachments = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "consultation_consent_flags",
                    joinColumns = @JoinColumn(name = "consultation_id"))
    @Column(name = "consent_type")
    @Builder.Default
    private List<String> consentFlags = new ArrayList<>();

    @Column(name = "recording_url", columnDefinition = "TEXT")
    private String recordingUrl;

    @Column(name = "video_provider", length = 50)
    @Builder.Default
    private String videoProvider = "JITSI"; // Default to open-source solution

    @Column(name = "connection_quality")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ConnectionQuality connectionQuality = ConnectionQuality.GOOD;

    @Column(name = "client_rating")
    private Integer clientRating; // 1-5 stars

    @Column(name = "provider_rating")
    private Integer providerRating; // 1-5 stars

    @Column(name = "client_feedback", columnDefinition = "TEXT")
    private String clientFeedback;

    @Column(name = "provider_feedback", columnDefinition = "TEXT")
    private String providerFeedback;

    @Column(name = "technical_issues")
    @Builder.Default
    private Boolean technicalIssues = false;

    @Column(name = "reconnection_count")
    @Builder.Default
    private Integer reconnectionCount = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum ConsultationType {
        VIDEO_CALL,
        PHONE_CALL,
        IN_PERSON
    }

    public enum PaymentMethod {
        MTN_MOBILE_MONEY,
        AIRTEL_MONEY,
        BANK_TRANSFER,
        CREDIT_CARD
    }

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REFUNDED
    }

    public enum ConsultationModality {
        VIDEO,     // Video call
        CHAT,      // Text chat consultation
        IN_PERSON  // Physical visit
    }

    public enum ConsultationStatus {
        PENDING_PAYMENT,   // Payment not yet made
        SCHEDULED,         // Booked and paid, ready to start
        ACTIVE,            // Session in progress
        IN_PROGRESS,       // Alternative name for ACTIVE
        COMPLETED,         // Session completed normally (alias for ENDED)
        ENDED,             // Session completed normally
        CANCELLED,         // Session cancelled
        INTERRUPTED,       // Technical issues prevented completion
        NO_SHOW            // Client didn't join
    }

    public enum ConnectionQuality {
        EXCELLENT,    // No issues
        GOOD,         // Minor delays
        FAIR,         // Some interruptions
        POOR          // Significant issues
    }

    // Helper methods for lifecycle management
    public void startSession() {
        if (this.status == ConsultationStatus.SCHEDULED) {
            this.status = ConsultationStatus.ACTIVE;
            this.startTime = LocalDateTime.now();
            generateRoomId();
        }
    }

    public void endSession() {
        if (this.status == ConsultationStatus.ACTIVE) {
            this.status = ConsultationStatus.ENDED;
            this.endTime = LocalDateTime.now();
            this.durationMinutes = calculateDuration();
        }
    }

    public void cancelSession(String reason) {
        this.status = ConsultationStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
    }

    public void markInterrupted() {
        if (this.status == ConsultationStatus.ACTIVE) {
            this.status = ConsultationStatus.INTERRUPTED;
            this.technicalIssues = true;
        }
    }

    public void markNoShow() {
        this.status = ConsultationStatus.NO_SHOW;
        this.endTime = LocalDateTime.now();
    }

    public void addAttachment(String attachmentUrl) {
        this.attachments.add(attachmentUrl);
    }

    public void addConsentFlag(String consentType) {
        this.consentFlags.add(consentType);
    }

    public void updateConnectionQuality(ConnectionQuality quality) {
        this.connectionQuality = quality;
    }

    public void incrementReconnectionCount() {
        this.reconnectionCount++;
    }

    public void addProviderNotes(String notes) {
        this.providerNotes = (this.providerNotes != null ? this.providerNotes + "\n" : "") + notes;
    }

    public void addClientNotes(String notes) {
        this.clientNotes = (this.clientNotes != null ? this.clientNotes + "\n" : "") + notes;
    }

    public void setActualStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setActualEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    private void generateRoomId() {
        if (this.roomId == null) {
            this.roomId = "consultation-" + this.appointment.getId() + "-" +
                         System.currentTimeMillis();
        }
    }

    private Integer calculateDuration() {
        if (this.startTime != null && this.endTime != null) {
            return (int) (this.endTime.toEpochSecond(java.time.ZoneOffset.UTC) - 
                         this.startTime.toEpochSecond(java.time.ZoneOffset.UTC)) / 60;
        }
        return null;
    }

    // Validation methods
    public boolean canStart() {
        return this.status == ConsultationStatus.SCHEDULED && 
               this.startTime != null && 
               this.startTime.isBefore(LocalDateTime.now().plusMinutes(15)); // Allow 15 min early join
    }

    public boolean canEnd() {
        return this.status == ConsultationStatus.ACTIVE;
    }

    public boolean isVideoSession() {
        return this.modality == ConsultationModality.VIDEO;
    }

    public boolean requiresRecordingConsent() {
        return this.modality == ConsultationModality.VIDEO || 
               this.modality == ConsultationModality.CHAT;
    }
}
