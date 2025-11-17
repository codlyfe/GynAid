package com.gynaid.backend.service;

import com.gynaid.backend.entity.Consultation;
import com.gynaid.backend.entity.HealthcareProvider;
import com.gynaid.backend.entity.User;
import com.gynaid.backend.repository.ConsultationRepository;
import com.gynaid.backend.repository.HealthcareProviderRepository;
import com.gynaid.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final HealthcareProviderRepository providerRepository;
    private final UserRepository userRepository;

    private static final BigDecimal APP_FEE_PERCENTAGE = new BigDecimal("0.10"); // 10%

    @Transactional
    public BookingResponse bookConsultation(BookConsultationRequest request, String userEmail) {
        try {
            User client = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

            HealthcareProvider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

            // Calculate fees
            BigDecimal consultationFee = BigDecimal.valueOf(provider.getConsultationFee());
            BigDecimal appFee = consultationFee.multiply(APP_FEE_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalAmount = consultationFee.add(appFee);

            // Create consultation with proper enum conversion
            Consultation consultation = Consultation.builder()
                .client(client)
                .provider(provider.getUser())
                .scheduledDateTime(request.getScheduledDateTime())
                .type(request.getType()) // This is now Consultation.ConsultationType
                .status(Consultation.ConsultationStatus.PENDING_PAYMENT)
                .consultationFee(consultationFee)
                .appFee(appFee)
                .totalAmount(totalAmount)
                .paymentStatus(Consultation.PaymentStatus.PENDING)
                .clientNotes(request.getClientNotes())
                .build();

            Consultation saved = consultationRepository.save(consultation);

            return BookingResponse.builder()
                .consultationId(saved.getId())
                .consultationFee(consultationFee)
                .appFee(appFee)
                .totalAmount(totalAmount)
                .success(true)
                .message("Consultation booked successfully. Please proceed with payment.")
                .build();

        } catch (Exception e) {
            log.error("Failed to book consultation", e);
            return BookingResponse.builder()
                .success(false)
                .message("Booking failed: " + e.getMessage())
                .build();
        }
    }

    @Transactional
    public PaymentResponse processPayment(Long consultationId, PaymentRequest request, String userEmail) {
        try {
            Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

            // Verify user owns this consultation
            if (!consultation.getClient().getEmail().equals(userEmail)) {
                throw new RuntimeException("Unauthorized access to consultation");
            }

            consultation.setPaymentMethod(request.getPaymentMethod());
            consultation.setPaymentStatus(Consultation.PaymentStatus.PROCESSING);
            consultationRepository.save(consultation);

            // Simulate payment processing (in real implementation, this would call PaymentService)
            // For now, we'll simulate a successful payment
            String transactionId = "TXN_" + System.currentTimeMillis();
            
            consultation.setPaymentStatus(Consultation.PaymentStatus.COMPLETED);
            consultation.setPaymentTransactionId(transactionId);
            consultation.setPaymentDateTime(java.time.LocalDateTime.now());
            consultation.setStatus(Consultation.ConsultationStatus.SCHEDULED);
            consultationRepository.save(consultation);

            log.info("Payment processed successfully for consultation: {}, transaction: {}", consultationId, transactionId);

            return PaymentResponse.builder()
                .success(true)
                .message("Payment successful. Consultation confirmed.")
                .transactionId(transactionId)
                .status(Consultation.PaymentStatus.COMPLETED)
                .build();

        } catch (Exception e) {
            log.error("Failed to process payment for consultation: {}", consultationId, e);
            
            // Update consultation status to failed if it exists
            try {
                Consultation consultation = consultationRepository.findById(consultationId).orElse(null);
                if (consultation != null) {
                    consultation.setPaymentStatus(Consultation.PaymentStatus.FAILED);
                    consultationRepository.save(consultation);
                }
            } catch (Exception updateEx) {
                log.error("Failed to update consultation payment status", updateEx);
            }

            return PaymentResponse.builder()
                .success(false)
                .message("Payment processing error: " + e.getMessage())
                .status(Consultation.PaymentStatus.FAILED)
                .build();
        }
    }

    /**
     * Generate secure room ID for consultation
     */
    @Transactional
    public String generateSecureRoomId(Long consultationId) {
        log.info("Generating secure room ID for consultation: {}", consultationId);
        
        Consultation consultation = consultationRepository.findById(consultationId)
            .orElseThrow(() -> new RuntimeException("Consultation not found"));
        
        // Generate secure room ID if not already exists
        if (consultation.getRoomId() == null) {
            String secureRoomId = generateSecureRoomId();
            consultation.setRoomId(secureRoomId);
            consultationRepository.save(consultation);
            
            log.info("Generated secure room ID: {} for consultation: {}", secureRoomId, consultationId);
            return secureRoomId;
        }
        
        return consultation.getRoomId();
    }

    /**
     * Start consultation session
     */
    @Transactional
    public Consultation startConsultationSession(Long consultationId, String userEmail) {
        log.info("Starting consultation session: {} for user: {}", consultationId, userEmail);
        
        Consultation consultation = consultationRepository.findById(consultationId)
            .orElseThrow(() -> new RuntimeException("Consultation not found"));
        
        // Verify user has access to this consultation
        if (!consultation.getClient().getEmail().equals(userEmail) &&
            !consultation.getProvider().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized access to consultation session");
        }
        
        // Check if consultation is in the right status
        if (consultation.getStatus() != Consultation.ConsultationStatus.SCHEDULED &&
            consultation.getStatus() != Consultation.ConsultationStatus.IN_PROGRESS) {
            throw new RuntimeException("Consultation is not ready to start");
        }
        
        // Generate room ID if not exists
        if (consultation.getRoomId() == null) {
            String roomId = generateSecureRoomId();
            consultation.setRoomId(roomId);
        }
        
        // Update status to in progress
        consultation.setStatus(Consultation.ConsultationStatus.IN_PROGRESS);
        consultation.setActualStartTime(LocalDateTime.now());
        
        Consultation updated = consultationRepository.save(consultation);
        
        log.info("Consultation session started successfully: {}", consultationId);
        return updated;
    }

    /**
     * End consultation session
     */
    @Transactional
    public Consultation endConsultationSession(Long consultationId, String userEmail, String notes) {
        log.info("Ending consultation session: {} for user: {}", consultationId, userEmail);
        
        Consultation consultation = consultationRepository.findById(consultationId)
            .orElseThrow(() -> new RuntimeException("Consultation not found"));
        
        // Verify user has access to this consultation
        if (!consultation.getClient().getEmail().equals(userEmail) &&
            !consultation.getProvider().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized access to consultation session");
        }
        
        // Update status to completed
        consultation.setStatus(Consultation.ConsultationStatus.COMPLETED);
        consultation.setActualEndTime(LocalDateTime.now());
        
        // Add provider notes if provided by provider
        if (notes != null && consultation.getProvider().getEmail().equals(userEmail)) {
            consultation.setProviderNotes(notes);
        }
        
        Consultation updated = consultationRepository.save(consultation);
        
        log.info("Consultation session ended successfully: {}", consultationId);
        return updated;
    }

    /**
     * Add consultation notes
     */
    @Transactional
    public void addConsultationNotes(Long consultationId, String userEmail, String notes, boolean isProvider) {
        log.info("Adding consultation notes for session: {} by user: {}", consultationId, userEmail);
        
        Consultation consultation = consultationRepository.findById(consultationId)
            .orElseThrow(() -> new RuntimeException("Consultation not found"));
        
        // Verify user has access to this consultation
        if (!consultation.getClient().getEmail().equals(userEmail) &&
            !consultation.getProvider().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized access to consultation session");
        }
        
        // Add notes based on who is adding them
        if (isProvider) {
            if (!consultation.getProvider().getEmail().equals(userEmail)) {
                throw new RuntimeException("Only providers can add provider notes");
            }
            consultation.setProviderNotes(notes);
        } else {
            if (!consultation.getClient().getEmail().equals(userEmail)) {
                throw new RuntimeException("Only clients can add client notes");
            }
            consultation.setClientNotes(notes);
        }
        
        consultationRepository.save(consultation);
        
        log.info("Consultation notes added successfully for session: {}", consultationId);
    }

    /**
     * Generate a secure random room ID
     */
    private String generateSecureRoomId() {
        // Generate a secure room ID using UUID and timestamp
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "GYNAID_" + uuid.substring(0, 8) + "_" + timestamp.substring(timestamp.length() - 6);
    }

    public PaymentMethodsResponse getPaymentMethods(Long consultationId) {
        Consultation consultation = consultationRepository.findById(consultationId)
            .orElseThrow(() -> new RuntimeException("Consultation not found"));

        List<PaymentMethodOption> methods = Arrays.asList(
            PaymentMethodOption.builder()
                .method(Consultation.PaymentMethod.MTN_MOBILE_MONEY)
                .displayName("MTN Mobile Money")
                .description("Pay using your MTN Mobile Money account")
                .enabled(true)
                .instructions("Enter your MTN Mobile Money number")
                .build(),
            
            PaymentMethodOption.builder()
                .method(Consultation.PaymentMethod.AIRTEL_MONEY)
                .displayName("Airtel Money")
                .description("Pay using your Airtel Money account")
                .enabled(true)
                .instructions("Enter your Airtel Money number")
                .build(),
            
            PaymentMethodOption.builder()
                .method(Consultation.PaymentMethod.BANK_TRANSFER)
                .displayName("Bank Transfer")
                .description("Transfer to our bank account")
                .enabled(true)
                .instructions("Transfer to Account: 1234567890, Bank: Stanbic Bank Uganda")
                .build(),
            
            PaymentMethodOption.builder()
                .method(Consultation.PaymentMethod.CREDIT_CARD)
                .displayName("Credit/Debit Card")
                .description("Pay with Visa or Mastercard")
                .enabled(true)
                .instructions("Secure card payment processing")
                .build()
        );

        return PaymentMethodsResponse.builder()
            .totalAmount(consultation.getTotalAmount())
            .availableMethods(methods)
            .build();
    }

    // Session Management Methods
    
    @Transactional
    public SessionStartResponse startConsultationSessionWithResponse(Long consultationId, String userEmail) {
        try {
            Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));
            
            // Verify user has permission to start session
            if (!consultation.getClient().getEmail().equals(userEmail) && 
                !consultation.getProvider().getEmail().equals(userEmail)) {
                throw new RuntimeException("Unauthorized to start this consultation");
            }
            
            // Check if session can start
            if (!consultation.canStart()) {
                return SessionStartResponse.builder()
                    .success(false)
                    .message("Consultation cannot be started yet")
                    .allowedStartTime(consultation.getScheduledDateTime().minusMinutes(15))
                    .build();
            }
            
            // Start the session
            consultation.startSession();
            consultationRepository.save(consultation);
            
            log.info("Consultation session started: {}, room: {}", consultationId, consultation.getRoomId());
            
            return SessionStartResponse.builder()
                .success(true)
                .roomId(consultation.getRoomId())
                .videoProvider(consultation.getVideoProvider())
                .sessionUrl(generateSessionUrl(consultation.getRoomId()))
                .message("Consultation session started successfully")
                .build();
                
        } catch (Exception e) {
            log.error("Failed to start consultation session: {}", consultationId, e);
            return SessionStartResponse.builder()
                .success(false)
                .message("Failed to start session: " + e.getMessage())
                .build();
        }
    }
    
    @Transactional
    public SessionEndResponse endConsultationSession(Long consultationId, String userEmail, String notes, Integer rating) {
        try {
            Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));
            
            // Verify user has permission to end session
            if (!consultation.getClient().getEmail().equals(userEmail) && 
                !consultation.getProvider().getEmail().equals(userEmail)) {
                throw new RuntimeException("Unauthorized to end this consultation");
            }
            
            if (!consultation.canEnd()) {
                return SessionEndResponse.builder()
                    .success(false)
                    .message("Session cannot be ended in current state: " + consultation.getStatus())
                    .build();
            }
            
            // Add notes if provided
            if (notes != null && !notes.trim().isEmpty()) {
                if (consultation.getClient().getEmail().equals(userEmail)) {
                    consultation.addClientNotes(notes);
                } else if (consultation.getProvider().getEmail().equals(userEmail)) {
                    consultation.addProviderNotes(notes);
                }
            }
            
            // Add rating if provided
            if (rating != null && rating >= 1 && rating <= 5) {
                if (consultation.getClient().getEmail().equals(userEmail)) {
                    consultation.setClientRating(rating);
                } else if (consultation.getProvider().getEmail().equals(userEmail)) {
                    consultation.setProviderRating(rating);
                }
            }
            
            // End the session
            consultation.endSession();
            consultationRepository.save(consultation);
            
            log.info("Consultation session ended: {}, duration: {} minutes", consultationId, consultation.getDurationMinutes());
            
            return SessionEndResponse.builder()
                .success(true)
                .durationMinutes(consultation.getDurationMinutes())
                .endedAt(consultation.getEndTime())
                .message("Consultation session ended successfully")
                .build();
                
        } catch (Exception e) {
            log.error("Failed to end consultation session: {}", consultationId, e);
            return SessionEndResponse.builder()
                .success(false)
                .message("Failed to end session: " + e.getMessage())
                .build();
        }
    }
    
    @Transactional
    public CancelConsultationResponse cancelConsultation(Long consultationId, String userEmail, String reason) {
        try {
            Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));
            
            // Verify user can cancel
            if (!consultation.getClient().getEmail().equals(userEmail) && 
                !consultation.getProvider().getEmail().equals(userEmail)) {
                throw new RuntimeException("Unauthorized to cancel this consultation");
            }
            
            // Check if consultation can be cancelled
            if (consultation.getStatus() == Consultation.ConsultationStatus.ACTIVE ||
                consultation.getStatus() == Consultation.ConsultationStatus.ENDED) {
                return CancelConsultationResponse.builder()
                    .success(false)
                    .message("Cannot cancel an active or completed consultation")
                    .build();
            }
            
            // Cancel the consultation
            consultation.cancelSession(reason);
            consultationRepository.save(consultation);
            
            log.info("Consultation cancelled: {}, reason: {}", consultationId, reason);
            
            return CancelConsultationResponse.builder()
                .success(true)
                .message("Consultation cancelled successfully")
                .cancelledAt(consultation.getEndTime())
                .build();
                
        } catch (Exception e) {
            log.error("Failed to cancel consultation: {}", consultationId, e);
            return CancelConsultationResponse.builder()
                .success(false)
                .message("Failed to cancel consultation: " + e.getMessage())
                .build();
        }
    }
    
    @Transactional
    public RescheduleConsultationResponse rescheduleConsultation(Long consultationId, String userEmail, LocalDateTime newDateTime) {
        try {
            Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));
            
            // Verify user can reschedule
            if (!consultation.getClient().getEmail().equals(userEmail)) {
                throw new RuntimeException("Only clients can reschedule consultations");
            }
            
            // Check if consultation can be rescheduled
            if (consultation.getStatus() == Consultation.ConsultationStatus.ACTIVE ||
                consultation.getStatus() == Consultation.ConsultationStatus.ENDED) {
                return RescheduleConsultationResponse.builder()
                    .success(false)
                    .message("Cannot reschedule an active or completed consultation")
                    .build();
            }
            
            // Validate new date time is in the future
            if (newDateTime.isBefore(LocalDateTime.now().plusHours(1))) {
                return RescheduleConsultationResponse.builder()
                    .success(false)
                    .message("New appointment time must be at least 1 hour in the future")
                    .build();
            }
            
            // Update the scheduled time
            consultation.setScheduledDateTime(newDateTime);
            consultationRepository.save(consultation);
            
            log.info("Consultation rescheduled: {} to {}", consultationId, newDateTime);
            
            return RescheduleConsultationResponse.builder()
                .success(true)
                .newDateTime(newDateTime)
                .message("Consultation rescheduled successfully")
                .build();
                
        } catch (Exception e) {
            log.error("Failed to reschedule consultation: {}", consultationId, e);
            return RescheduleConsultationResponse.builder()
                .success(false)
                .message("Failed to reschedule consultation: " + e.getMessage())
                .build();
        }
    }
    
    private String generateSessionUrl(String roomId) {
        // Generate session URL based on video provider
        return "https://video.gynaid.com/room/" + roomId;
    }

    // Request/Response DTOs
    public static class BookConsultationRequest {
        private Long providerId;
        private java.time.LocalDateTime scheduledDateTime;
        private Consultation.ConsultationType type; // Changed to enum
        private String clientNotes;

        // Getters and setters
        public Long getProviderId() { return providerId; }
        public void setProviderId(Long providerId) { this.providerId = providerId; }
        
        public java.time.LocalDateTime getScheduledDateTime() { return scheduledDateTime; }
        public void setScheduledDateTime(java.time.LocalDateTime scheduledDateTime) { this.scheduledDateTime = scheduledDateTime; }
        
        public Consultation.ConsultationType getType() { return type; }
        public void setType(Consultation.ConsultationType type) { this.type = type; }
        
        public String getClientNotes() { return clientNotes; }
        public void setClientNotes(String clientNotes) { this.clientNotes = clientNotes; }
    }

    public static class PaymentRequest {
        private Consultation.PaymentMethod paymentMethod;

        public Consultation.PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(Consultation.PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    }

    public static class BookingResponse {
        private Long consultationId;
        private BigDecimal consultationFee;
        private BigDecimal appFee;
        private BigDecimal totalAmount;
        private boolean success;
        private String message;

        public static class Builder {
            private BookingResponse result = new BookingResponse();

            public Builder consultationId(Long consultationId) {
                result.consultationId = consultationId;
                return this;
            }

            public Builder consultationFee(BigDecimal consultationFee) {
                result.consultationFee = consultationFee;
                return this;
            }

            public Builder appFee(BigDecimal appFee) {
                result.appFee = appFee;
                return this;
            }

            public Builder totalAmount(BigDecimal totalAmount) {
                result.totalAmount = totalAmount;
                return this;
            }

            public Builder success(boolean success) {
                result.success = success;
                return this;
            }

            public Builder message(String message) {
                result.message = message;
                return this;
            }

            public BookingResponse build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public Long getConsultationId() { return consultationId; }
        public BigDecimal getConsultationFee() { return consultationFee; }
        public BigDecimal getAppFee() { return appFee; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    public static class PaymentResponse {
        private boolean success;
        private String message;
        private String transactionId;
        private Consultation.PaymentStatus status;

        public static class Builder {
            private PaymentResponse result = new PaymentResponse();

            public Builder success(boolean success) {
                result.success = success;
                return this;
            }

            public Builder message(String message) {
                result.message = message;
                return this;
            }

            public Builder transactionId(String transactionId) {
                result.transactionId = transactionId;
                return this;
            }

            public Builder status(Consultation.PaymentStatus status) {
                result.status = status;
                return this;
            }

            public PaymentResponse build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getTransactionId() { return transactionId; }
        public Consultation.PaymentStatus getStatus() { return status; }
    }

    public static class PaymentMethodOption {
        private Consultation.PaymentMethod method;
        private String displayName;
        private String description;
        private boolean enabled;
        private String instructions;

        public static class Builder {
            private PaymentMethodOption result = new PaymentMethodOption();

            public Builder method(Consultation.PaymentMethod method) {
                result.method = method;
                return this;
            }

            public Builder displayName(String displayName) {
                result.displayName = displayName;
                return this;
            }

            public Builder description(String description) {
                result.description = description;
                return this;
            }

            public Builder enabled(boolean enabled) {
                result.enabled = enabled;
                return this;
            }

            public Builder instructions(String instructions) {
                result.instructions = instructions;
                return this;
            }

            public PaymentMethodOption build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public Consultation.PaymentMethod getMethod() { return method; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public boolean isEnabled() { return enabled; }
        public String getInstructions() { return instructions; }
    }

    public static class PaymentMethodsResponse {
        private BigDecimal totalAmount;
        private List<PaymentMethodOption> availableMethods;

        public static class Builder {
            private PaymentMethodsResponse result = new PaymentMethodsResponse();

            public Builder totalAmount(BigDecimal totalAmount) {
                result.totalAmount = totalAmount;
                return this;
            }

            public Builder availableMethods(List<PaymentMethodOption> availableMethods) {
                result.availableMethods = availableMethods;
                return this;
            }

            public PaymentMethodsResponse build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public BigDecimal getTotalAmount() { return totalAmount; }
        public List<PaymentMethodOption> getAvailableMethods() { return availableMethods; }
    }

    // Session Management Response DTOs
    public static class SessionStartResponse {
        private boolean success;
        private String message;
        private String roomId;
        private String videoProvider;
        private String sessionUrl;
        private LocalDateTime allowedStartTime;

        public static class Builder {
            private SessionStartResponse result = new SessionStartResponse();

            public Builder success(boolean success) {
                result.success = success;
                return this;
            }

            public Builder message(String message) {
                result.message = message;
                return this;
            }

            public Builder roomId(String roomId) {
                result.roomId = roomId;
                return this;
            }

            public Builder videoProvider(String videoProvider) {
                result.videoProvider = videoProvider;
                return this;
            }

            public Builder sessionUrl(String sessionUrl) {
                result.sessionUrl = sessionUrl;
                return this;
            }

            public Builder allowedStartTime(LocalDateTime allowedStartTime) {
                result.allowedStartTime = allowedStartTime;
                return this;
            }

            public SessionStartResponse build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getRoomId() { return roomId; }
        public String getVideoProvider() { return videoProvider; }
        public String getSessionUrl() { return sessionUrl; }
        public LocalDateTime getAllowedStartTime() { return allowedStartTime; }
    }

    public static class SessionEndResponse {
        private boolean success;
        private String message;
        private Integer durationMinutes;
        private LocalDateTime endedAt;

        public static class Builder {
            private SessionEndResponse result = new SessionEndResponse();

            public Builder success(boolean success) {
                result.success = success;
                return this;
            }

            public Builder message(String message) {
                result.message = message;
                return this;
            }

            public Builder durationMinutes(Integer durationMinutes) {
                result.durationMinutes = durationMinutes;
                return this;
            }

            public Builder endedAt(LocalDateTime endedAt) {
                result.endedAt = endedAt;
                return this;
            }

            public SessionEndResponse build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public LocalDateTime getEndedAt() { return endedAt; }
    }

    public static class CancelConsultationResponse {
        private boolean success;
        private String message;
        private LocalDateTime cancelledAt;

        public static class Builder {
            private CancelConsultationResponse result = new CancelConsultationResponse();

            public Builder success(boolean success) {
                result.success = success;
                return this;
            }

            public Builder message(String message) {
                result.message = message;
                return this;
            }

            public Builder cancelledAt(LocalDateTime cancelledAt) {
                result.cancelledAt = cancelledAt;
                return this;
            }

            public CancelConsultationResponse build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public LocalDateTime getCancelledAt() { return cancelledAt; }
    }

    public static class RescheduleConsultationResponse {
        private boolean success;
        private String message;
        private LocalDateTime newDateTime;

        public static class Builder {
            private RescheduleConsultationResponse result = new RescheduleConsultationResponse();

            public Builder success(boolean success) {
                result.success = success;
                return this;
            }

            public Builder message(String message) {
                result.message = message;
                return this;
            }

            public Builder newDateTime(LocalDateTime newDateTime) {
                result.newDateTime = newDateTime;
                return this;
            }

            public RescheduleConsultationResponse build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public LocalDateTime getNewDateTime() { return newDateTime; }
    }
}
