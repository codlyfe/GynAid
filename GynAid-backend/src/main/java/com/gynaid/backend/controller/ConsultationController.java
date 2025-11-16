package com.gynaid.backend.controller;

import com.gynaid.backend.entity.Consultation;
import com.gynaid.backend.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/consultations")
@Slf4j
public class ConsultationController {

    private final ConsultationService consultationService;

    // Explicit constructor for dependency injection
    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @PostMapping("/book")
    public ResponseEntity<Map<String, Object>> bookConsultation(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        try {
            // Convert request to service DTO
            ConsultationService.BookConsultationRequest serviceRequest = new ConsultationService.BookConsultationRequest();
            serviceRequest.setProviderId(Long.valueOf(request.get("providerId").toString()));
            serviceRequest.setScheduledDateTime(LocalDateTime.parse(request.get("scheduledDateTime").toString()));
            
            // Handle enum conversion
            String typeStr = (String) request.get("type");
            if (typeStr != null) {
                serviceRequest.setType(Consultation.ConsultationType.valueOf(typeStr));
            }
            
            serviceRequest.setClientNotes((String) request.get("clientNotes"));
            
            ConsultationService.BookingResponse response = consultationService.bookConsultation(serviceRequest, authentication.getName());
            
            Map<String, Object> result = new HashMap<>();
            result.put("consultationId", response.getConsultationId());
            result.put("consultationFee", response.getConsultationFee());
            result.put("appFee", response.getAppFee());
            result.put("totalAmount", response.getTotalAmount());
            result.put("success", response.isSuccess());
            result.put("message", response.getMessage());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error booking consultation", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Booking failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{consultationId}/payment")
    public ResponseEntity<Map<String, Object>> processPayment(
            @PathVariable Long consultationId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        try {
            // Convert request to service DTO
            ConsultationService.PaymentRequest serviceRequest = new ConsultationService.PaymentRequest();
            String paymentMethodStr = (String) request.get("paymentMethod");
            if (paymentMethodStr != null) {
                serviceRequest.setPaymentMethod(Consultation.PaymentMethod.valueOf(paymentMethodStr));
            }
            
            ConsultationService.PaymentResponse response = consultationService.processPayment(consultationId, serviceRequest, authentication.getName());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response.isSuccess());
            result.put("message", response.getMessage());
            result.put("transactionId", response.getTransactionId());
            result.put("status", response.getStatus().name());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error processing payment", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Payment processing failed: " + e.getMessage());
            error.put("status", "FAILED");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{consultationId}/payment-methods")
    public ResponseEntity<Map<String, Object>> getPaymentMethods(@PathVariable Long consultationId) {
        try {
            ConsultationService.PaymentMethodsResponse response = consultationService.getPaymentMethods(consultationId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalAmount", response.getTotalAmount());
            result.put("availableMethods", response.getAvailableMethods());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error getting payment methods", e);
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Failed to get payment methods: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
