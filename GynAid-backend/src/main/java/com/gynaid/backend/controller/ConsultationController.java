package com.gynaid.backend.controller;

import com.gynaid.backend.entity.Consultation;
import com.gynaid.backend.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @PostMapping("/book")
    public ResponseEntity<BookingResponse> bookConsultation(
            @RequestBody BookConsultationRequest request,
            Authentication authentication) {
        
        BookingResponse response = consultationService.bookConsultation(request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{consultationId}/payment")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable Long consultationId,
            @RequestBody PaymentRequest request,
            Authentication authentication) {
        
        PaymentResponse response = consultationService.processPayment(consultationId, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{consultationId}/payment-methods")
    public ResponseEntity<PaymentMethodsResponse> getPaymentMethods(@PathVariable Long consultationId) {
        PaymentMethodsResponse response = consultationService.getPaymentMethods(consultationId);
        return ResponseEntity.ok(response);
    }

    // DTOs
    @lombok.Data
    public static class BookConsultationRequest {
        private Long providerId;
        private LocalDateTime scheduledDateTime;
        private Consultation.ConsultationType type;
        private String clientNotes;
    }

    @lombok.Data
    @lombok.Builder
    public static class BookingResponse {
        private Long consultationId;
        private BigDecimal consultationFee;
        private BigDecimal appFee;
        private BigDecimal totalAmount;
        private String message;
        private boolean success;
    }

    @lombok.Data
    public static class PaymentRequest {
        private Consultation.PaymentMethod paymentMethod;
        private String phoneNumber; // For mobile money
        private String bankAccount; // For bank transfer
        private String cardToken; // For card payments
    }

    @lombok.Data
    @lombok.Builder
    public static class PaymentResponse {
        private boolean success;
        private String message;
        private String transactionId;
        private String paymentInstructions;
        private Consultation.PaymentStatus status;
    }

    @lombok.Data
    @lombok.Builder
    public static class PaymentMethodsResponse {
        private BigDecimal totalAmount;
        private java.util.List<PaymentMethodOption> availableMethods;
    }

    @lombok.Data
    @lombok.Builder
    public static class PaymentMethodOption {
        private Consultation.PaymentMethod method;
        private String displayName;
        private String description;
        private boolean enabled;
        private String instructions;
    }
}
