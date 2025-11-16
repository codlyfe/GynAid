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
                .provider(provider)
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
}
