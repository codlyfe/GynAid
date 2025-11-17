package com.gynaid.backend.controller;

import com.gynaid.backend.entity.Appointment;
import com.gynaid.backend.entity.Payment;
import com.gynaid.backend.entity.User;
import com.gynaid.backend.repository.PaymentRepository;
import com.gynaid.backend.service.AppointmentService;
import com.gynaid.backend.service.PaymentService;
import com.gynaid.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

/**
 * Payment Controller for handling Stripe payments
 */
@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final AppointmentService appointmentService;
    private final UserService userService;

    /**
     * Create payment intent for appointment
     */
    @PostMapping("/create-intent")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(
            @RequestBody CreatePaymentIntentRequest request,
            Authentication authentication) {
        
        try {
            User client = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Appointment appointment = appointmentService.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Verify the client owns this appointment
            if (!appointment.getClient().getId().equals(client.getId())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Unauthorized",
                    "message", "You can only pay for your own appointments"
                ));
            }
            
            PaymentService.PaymentIntentResponse response = paymentService.createPaymentIntent(
                appointment, 
                client, 
                request.getPaymentMethod()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "paymentId", response.getPaymentId(),
                "clientSecret", response.getClientSecret(),
                "publishableKey", response.getPublishableKey(),
                "amount", response.getAmount(),
                "currency", response.getCurrency()
            ));
            
        } catch (Exception e) {
            log.error("Error creating payment intent", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Payment intent creation failed",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Get payment status
     */
    @GetMapping("/{paymentId}/status")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(
            @PathVariable Long paymentId,
            Authentication authentication) {
        
        try {
            User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            PaymentService.PaymentStatusResponse paymentResponse = paymentService.getPaymentStatus(paymentId);
            Payment payment = paymentRepository.findById(paymentResponse.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            // Check authorization - client must own the appointment or be admin
            boolean hasAccess = user.getRole() == User.UserRole.ADMIN ||
                              payment.getAppointment().getClient().getId().equals(user.getId()) ||
                              payment.getAppointment().getProvider().getId().equals(user.getId());
            
            if (!hasAccess) {
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Access denied"
                ));
            }
            
            PaymentService.PaymentStatusResponse response = paymentService.getPaymentStatus(paymentId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "paymentId", response.getPaymentId(),
                "status", response.getStatus(),
                "amount", response.getAmount(),
                "currency", response.getCurrency(),
                "paymentMethod", response.getPaymentMethod(),
                "receiptUrl", response.getReceiptUrl(),
                "createdAt", response.getCreatedAt()
            ));
            
        } catch (Exception e) {
            log.error("Error getting payment status", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to get payment status",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Process refund (admin only)
     */
    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> processRefund(
            @PathVariable Long paymentId,
            @RequestBody RefundRequest request,
            Authentication authentication) {
        
        try {
            User admin = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            PaymentService.RefundResponse response = paymentService.processRefund(
                paymentId, 
                request.getAmount(), 
                request.getReason()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "refundId", response.getRefundId(),
                "amount", response.getAmount(),
                "status", response.getStatus(),
                "receiptUrl", response.getReceiptUrl()
            ));
            
        } catch (Exception e) {
            log.error("Error processing refund", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Refund processing failed",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Stripe webhook endpoint
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody PaymentService.WebhookEvent webhookEvent,
            @RequestHeader("Stripe-Signature") String signature) {
        
        try {
            // In production, verify webhook signature here
            // For now, we'll process the event directly
            log.info("Received webhook: {}", webhookEvent.getType());
            
            paymentService.handleWebhookEvent(webhookEvent);
            
            return ResponseEntity.ok("Webhook processed successfully");
            
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.badRequest().body("Webhook processing failed");
        }
    }

    // Request/Response DTOs
    @lombok.Data
    public static class CreatePaymentIntentRequest {
        private Long appointmentId;
        private PaymentService.PaymentMethod paymentMethod;
    }

    @lombok.Data
    public static class RefundRequest {
        private BigDecimal amount;
        private String reason;
    }
}