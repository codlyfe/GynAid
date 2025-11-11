package com.gynassist.backend.service;

import com.gynassist.backend.entity.Consultation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    public PaymentResult processPayment(
            BigDecimal amount,
            Consultation.PaymentMethod method,
            String phoneNumber,
            String bankAccount,
            String cardToken) {
        
        try {
            switch (method) {
                case MTN_MOBILE_MONEY:
                    return processMTNMobileMoney(amount, phoneNumber);
                case AIRTEL_MONEY:
                    return processAirtelMoney(amount, phoneNumber);
                case BANK_TRANSFER:
                    return processBankTransfer(amount, bankAccount);
                case CREDIT_CARD:
                    return processCreditCard(amount, cardToken);
                default:
                    return PaymentResult.builder()
                        .success(false)
                        .errorMessage("Unsupported payment method")
                        .build();
            }
        } catch (Exception e) {
            return PaymentResult.builder()
                .success(false)
                .errorMessage("Payment processing failed: " + e.getMessage())
                .build();
        }
    }

    private PaymentResult processMTNMobileMoney(BigDecimal amount, String phoneNumber) {
        // Simulate MTN Mobile Money API integration
        // In production, integrate with MTN MoMo API
        
        if (phoneNumber == null || !phoneNumber.startsWith("256")) {
            return PaymentResult.builder()
                .success(false)
                .errorMessage("Invalid MTN Mobile Money number")
                .build();
        }

        // Simulate API call
        String transactionId = "MTN_" + UUID.randomUUID().toString().substring(0, 8);
        
        return PaymentResult.builder()
            .success(true)
            .transactionId(transactionId)
            .message("MTN Mobile Money payment successful")
            .build();
    }

    private PaymentResult processAirtelMoney(BigDecimal amount, String phoneNumber) {
        // Simulate Airtel Money API integration
        
        if (phoneNumber == null || !phoneNumber.startsWith("256")) {
            return PaymentResult.builder()
                .success(false)
                .errorMessage("Invalid Airtel Money number")
                .build();
        }

        String transactionId = "AIRTEL_" + UUID.randomUUID().toString().substring(0, 8);
        
        return PaymentResult.builder()
            .success(true)
            .transactionId(transactionId)
            .message("Airtel Money payment successful")
            .build();
    }

    private PaymentResult processBankTransfer(BigDecimal amount, String bankAccount) {
        // Simulate bank transfer processing
        
        String transactionId = "BANK_" + UUID.randomUUID().toString().substring(0, 8);
        
        return PaymentResult.builder()
            .success(true)
            .transactionId(transactionId)
            .message("Bank transfer initiated. Please allow 1-2 business days for processing.")
            .build();
    }

    private PaymentResult processCreditCard(BigDecimal amount, String cardToken) {
        // Simulate credit card processing (Stripe/other gateway)
        
        if (cardToken == null || cardToken.length() < 10) {
            return PaymentResult.builder()
                .success(false)
                .errorMessage("Invalid card information")
                .build();
        }

        String transactionId = "CARD_" + UUID.randomUUID().toString().substring(0, 8);
        
        return PaymentResult.builder()
            .success(true)
            .transactionId(transactionId)
            .message("Credit card payment successful")
            .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class PaymentResult {
        private boolean success;
        private String transactionId;
        private String message;
        private String errorMessage;
    }
}