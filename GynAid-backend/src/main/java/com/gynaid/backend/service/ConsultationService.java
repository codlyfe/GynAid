package com.gynaid.backend.service;

import com.gynaid.backend.controller.ConsultationController.*;
import com.gynaid.backend.entity.Consultation;
import com.gynaid.backend.entity.HealthcareProvider;
import com.gynaid.backend.entity.User;
import com.gynaid.backend.repository.ConsultationRepository;
import com.gynaid.backend.repository.HealthcareProviderRepository;
import com.gynaid.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final HealthcareProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

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

            // Create consultation
            Consultation consultation = Consultation.builder()
                .client(client)
                .provider(provider)
                .scheduledDateTime(request.getScheduledDateTime())
                .type(request.getType())
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

            // Process payment based on method
            PaymentService.PaymentResult result = paymentService.processPayment(
                consultation.getTotalAmount(),
                request.getPaymentMethod(),
                request.getPhoneNumber(),
                request.getBankAccount(),
                request.getCardToken()
            );

            if (result.isSuccess()) {
                consultation.setPaymentStatus(Consultation.PaymentStatus.COMPLETED);
                consultation.setPaymentTransactionId(result.getTransactionId());
                consultation.setPaymentDateTime(java.time.LocalDateTime.now());
                consultation.setStatus(Consultation.ConsultationStatus.SCHEDULED);
                consultationRepository.save(consultation);

                return PaymentResponse.builder()
                    .success(true)
                    .message("Payment successful. Consultation confirmed.")
                    .transactionId(result.getTransactionId())
                    .status(Consultation.PaymentStatus.COMPLETED)
                    .build();
            } else {
                consultation.setPaymentStatus(Consultation.PaymentStatus.FAILED);
                consultationRepository.save(consultation);

                return PaymentResponse.builder()
                    .success(false)
                    .message("Payment failed: " + result.getErrorMessage())
                    .status(Consultation.PaymentStatus.FAILED)
                    .build();
            }

        } catch (Exception e) {
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
}
