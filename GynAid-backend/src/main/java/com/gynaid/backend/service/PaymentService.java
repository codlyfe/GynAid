package com.gynaid.backend.service;

import com.gynaid.backend.entity.Appointment;
import com.gynaid.backend.entity.Consultation;
import com.gynaid.backend.entity.Payment;
import com.gynaid.backend.entity.User;
import com.gynaid.backend.repository.AppointmentRepository;
import com.gynaid.backend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Payment Service for processing Stripe payments
 * Handles payment intents, webhook verification, and refund processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    private final AppointmentRepository appointmentRepository; // Added missing repository
    
    // Stripe Configuration (these would come from environment variables in production)
    private static final String STRIPE_SECRET_KEY = "sk_test_..."; // Should be from env
    private static final String STRIPE_WEBHOOK_SECRET = "whsec_..."; // Should be from env
    private static final String STRIPE_BASE_URL = "https://api.stripe.com/v1";

    /**
     * Create payment intent for appointment
     */
    @Transactional
    public PaymentIntentResponse createPaymentIntent(
            Appointment appointment, 
            User client, 
            PaymentMethod paymentMethod) {
        
        log.info("Creating payment intent for appointment: {}", appointment.getId());
        
        try {
            // Calculate amounts
            BigDecimal consultationFee = BigDecimal.valueOf(250000); // UGX 250,000 default
            BigDecimal platformFee = consultationFee.multiply(BigDecimal.valueOf(0.10)); // 10% platform fee
            BigDecimal totalAmount = consultationFee.add(platformFee);
            
            // Create idempotency key to prevent duplicate payments
            String idempotencyKey = generateIdempotencyKey(appointment.getId(), client.getId());
            
            // Create Stripe payment intent
            StripePaymentIntent stripeIntent = createStripePaymentIntent(
                totalAmount, 
                "UGX", 
                "Consultation with " + appointment.getProvider().getFirstName() + " " + appointment.getProvider().getLastName(),
                idempotencyKey
            );
            
            // Create local payment record
            Payment payment = Payment.builder()
                .appointment(appointment)
                .amount(totalAmount)
                .currency("UGX")
                .paymentMethod(mapToStripeMethod(paymentMethod))
                .stripePaymentIntentId(stripeIntent.id)
                .stripeCustomerId(stripeIntent.customerId)
                .idempotencyKey(idempotencyKey)
                .platformFee(platformFee)
                .providerShare(consultationFee)
                .status(Payment.PaymentStatus.INITIATED)
                .build();
            
            Payment savedPayment = paymentRepository.save(payment);
            
            return PaymentIntentResponse.builder()
                .paymentId(savedPayment.getId())
                .clientSecret(stripeIntent.clientSecret)
                .publishableKey(getPublishableKey()) // In production, this would be configured
                .amount(totalAmount)
                .currency("UGX")
                .build();
                
        } catch (Exception e) {
            log.error("Error creating payment intent", e);
            throw new RuntimeException("Failed to create payment intent: " + e.getMessage());
        }
    }

    /**
     * Handle Stripe webhook events
     */
    @Transactional
    public void handleWebhookEvent(WebhookEvent webhookEvent) {
        log.info("Processing webhook event: {}", webhookEvent.type);
        
        try {
            switch (webhookEvent.type) {
                case "payment_intent.succeeded":
                    handlePaymentSucceeded(webhookEvent.data.object);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentFailed(webhookEvent.data.object);
                    break;
                case "payment_intent.canceled":
                    handlePaymentCanceled(webhookEvent.data.object);
                    break;
                case "charge.refunded":
                    handleRefundProcessed(webhookEvent.data.object);
                    break;
                default:
                    log.warn("Unhandled webhook event type: {}", webhookEvent.type);
            }
            
        } catch (Exception e) {
            log.error("Error processing webhook event: {}", webhookEvent.type, e);
            throw new RuntimeException("Webhook processing failed");
        }
    }

    /**
     * Update appointment status when payment succeeds
     */
    @Transactional
    public void handlePaymentSucceeded(String stripePaymentIntentId) {
        log.info("Processing payment success for intent: {}", stripePaymentIntentId);
        
        Payment payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        // Update payment status
        payment.markSucceeded();
        paymentRepository.save(payment);
        
        // Update appointment status to approved and paid
        Appointment appointment = payment.getAppointment();
        appointment.setPaymentStatus(Appointment.PaymentStatus.PAID);
        
        // Auto-approve appointment when payment is successful
        appointment.setStatus(Appointment.AppointmentStatus.APPROVED);
        
        // Save appointment
        appointmentRepository.save(appointment);
        
        log.info("Appointment {} approved and payment completed", appointment.getId());
    }

    /**
     * Handle payment failure
     */
    @Transactional
    public void handlePaymentFailed(String stripePaymentIntentId) {
        log.info("Processing payment failure for intent: {}", stripePaymentIntentId);
        
        Payment payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setStatus(Payment.PaymentStatus.FAILED);
        paymentRepository.save(payment);
        
        // Update appointment payment status
        Appointment appointment = payment.getAppointment();
        appointment.setPaymentStatus(Appointment.PaymentStatus.FAILED);
        appointmentRepository.save(appointment);
        
        log.info("Payment failed for appointment: {}", appointment.getId());
    }

    /**
     * Handle payment cancellation
     */
    @Transactional
    public void handlePaymentCanceled(String stripePaymentIntentId) {
        log.info("Processing payment cancellation for intent: {}", stripePaymentIntentId);
        
        Payment payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setStatus(Payment.PaymentStatus.FAILED);
        paymentRepository.save(payment);
        
        // Cancel appointment as well
        Appointment appointment = payment.getAppointment();
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointment.setPaymentStatus(Appointment.PaymentStatus.UNPAID);
        appointmentRepository.save(appointment);
        
        log.info("Payment and appointment {} canceled", appointment.getId());
    }

    /**
     * Handle refund processing
     */
    @Transactional
    public void handleRefundProcessed(String stripeChargeId) {
        log.info("Processing refund for charge: {}", stripeChargeId);
        
        // Find payment by charge ID (would need to store charge ID in payment record)
        // For now, we'll use a simplified approach
        List<Payment> payments = paymentRepository.findByStripePaymentIntentIdContaining(stripeChargeId);
        
        for (Payment payment : payments) {
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            
            // Update appointment status
            Appointment appointment = payment.getAppointment();
            appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
            appointment.setPaymentStatus(Appointment.PaymentStatus.REFUNDED);
            appointmentRepository.save(appointment);
            
            log.info("Refund processed for appointment: {}", appointment.getId());
        }
    }

    /**
     * Process refund for appointment
     */
    @Transactional
    public RefundResponse processRefund(Long paymentId, BigDecimal amount, String reason) {
        log.info("Processing refund for payment: {}", paymentId);
        
        try {
            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            if (payment.getStatus() != Payment.PaymentStatus.SUCCEEDED) {
                throw new RuntimeException("Can only refund successful payments");
            }
            
            // Create Stripe refund
            StripeRefund stripeRefund = createStripeRefund(
                payment.getStripePaymentIntentId(),
                amount,
                reason
            );
            
            // Update payment record
            payment.refund();
            payment.setReceiptUrl(stripeRefund.receiptUrl);
            paymentRepository.save(payment);
            
            // Update appointment status if needed
            Appointment appointment = payment.getAppointment();
            appointment.refund();
            
            return RefundResponse.builder()
                .refundId(stripeRefund.id)
                .amount(amount)
                .status("PROCESSED")
                .receiptUrl(stripeRefund.receiptUrl)
                .build();
                
        } catch (Exception e) {
            log.error("Error processing refund", e);
            throw new RuntimeException("Refund processing failed: " + e.getMessage());
        }
    }

    /**
     * Get payment status
     */
    @Transactional(readOnly = true)
    public PaymentStatusResponse getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        return PaymentStatusResponse.builder()
            .paymentId(payment.getId())
            .status(payment.getStatus().name())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .paymentMethod(payment.getPaymentMethod())
            .receiptUrl(payment.getReceiptUrl())
            .createdAt(payment.getCreatedAt())
            .build();
    }

    // Helper methods for Stripe integration
    private StripePaymentIntent createStripePaymentIntent(
            BigDecimal amount, 
            String currency, 
            String description, 
            String idempotencyKey) {
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(STRIPE_SECRET_KEY);
        
        String body = String.format(
            "amount=%d&currency=%s&description=%s&automatic_payment_methods[enabled]=true",
            amount.multiply(new BigDecimal(100)).intValue(), // Convert to cents
            currency,
            description
        );
        
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            STRIPE_BASE_URL + "/payment_intents", 
            entity, 
            Map.class
        );
        
        @SuppressWarnings("unchecked")
        Map<String, Object> intent = response.getBody();
        
        return StripePaymentIntent.builder()
            .id(intent.get("id").toString())
            .clientSecret(intent.get("client_secret").toString())
            .customerId(intent.get("customer") != null ? intent.get("customer").toString() : null)
            .build();
    }
    
    private StripeRefund createStripeRefund(String paymentIntentId, BigDecimal amount, String reason) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(STRIPE_SECRET_KEY);
        
        String body = String.format(
            "payment_intent=%s&amount=%d&reason=%s",
            paymentIntentId,
            amount.multiply(new BigDecimal(100)).intValue(),
            reason
        );
        
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            STRIPE_BASE_URL + "/refunds", 
            entity, 
            Map.class
        );
        
        @SuppressWarnings("unchecked")
        Map<String, Object> refund = response.getBody();
        
        return StripeRefund.builder()
            .id(refund.get("id").toString())
            .receiptUrl(refund.get("receipt_url") != null ? refund.get("receipt_url").toString() : null)
            .build();
    }
    
    private void handlePaymentSucceeded(Map<String, Object> paymentIntent) {
        String paymentIntentId = paymentIntent.get("id").toString();
        
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.markSucceeded();
        payment.setReceiptUrl(getReceiptUrl(paymentIntentId));
        paymentRepository.save(payment);
        
        // Update appointment
        Appointment appointment = payment.getAppointment();
        appointment.markAsPaid();
    }
    
    private void handlePaymentFailed(Map<String, Object> paymentIntent) {
        String paymentIntentId = paymentIntent.get("id").toString();
        
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.markFailed();
        paymentRepository.save(payment);
    }
    
    private void handlePaymentCanceled(Map<String, Object> paymentIntent) {
        String paymentIntentId = paymentIntent.get("id").toString();
        
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.markFailed();
        paymentRepository.save(payment);
    }
    
    private void handleRefundProcessed(Map<String, Object> charge) {
        String paymentIntentId = charge.get("payment_intent").toString();
        
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.refund();
        paymentRepository.save(payment);
    }
    
    private String generateIdempotencyKey(Long appointmentId, Long clientId) {
        return String.format("appointment_%d_client_%d_%s", 
            appointmentId, clientId, UUID.randomUUID().toString());
    }
    
    private String mapToStripeMethod(PaymentMethod method) {
        return switch (method) {
            case MTN_MOBILE_MONEY -> "mobile_money";
            case AIRTEL_MONEY -> "mobile_money";
            case BANK_TRANSFER -> "bank_transfer";
            case CREDIT_CARD -> "card";
        };
    }
    
    private String getPublishableKey() {
        return "pk_test_..."; // Should be from environment configuration
    }
    
    private String getReceiptUrl(String paymentIntentId) {
        // In a real implementation, this would fetch the receipt URL from Stripe
        return "https://pay.stripe.com/receipts/" + paymentIntentId;
    }

    // Enums and DTOs
    public enum PaymentMethod {
        MTN_MOBILE_MONEY,
        AIRTEL_MONEY,
        BANK_TRANSFER,
        CREDIT_CARD
    }

    @lombok.Data
    @lombok.Builder
    public static class PaymentIntentResponse {
        private Long paymentId;
        private String clientSecret;
        private String publishableKey;
        private BigDecimal amount;
        private String currency;
    }

    @lombok.Data
    @lombok.Builder
    public static class RefundResponse {
        private String refundId;
        private BigDecimal amount;
        private String status;
        private String receiptUrl;
    }

    @lombok.Data
    @lombok.Builder
    public static class PaymentStatusResponse {
        private Long paymentId;
        private String status;
        private BigDecimal amount;
        private String currency;
        private String paymentMethod;
        private String receiptUrl;
        private LocalDateTime createdAt;
        
        // Add missing getPayment() method
        public PaymentStatusResponse getPayment() {
            return this;
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class StripePaymentIntent {
        private String id;
        private String clientSecret;
        private String customerId;
    }

    @lombok.Data
    @lombok.Builder
    public static class StripeRefund {
        private String id;
        private String receiptUrl;
    }

    @lombok.Data
    @lombok.Builder
    public static class WebhookEvent {
        private String type;
        private WebhookData data;
        private Long created;
    }

    @lombok.Data
    @lombok.Builder
    public static class WebhookData {
        private Map<String, Object> object;
    }
}
