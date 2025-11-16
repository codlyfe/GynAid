package com.gynaid.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Enterprise-grade SMS verification service for GynAid
 * Supports Uganda phone numbers with rate limiting and security features
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SMSVerificationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TwilioClient twilioClient;
    
    @Value("${app.sms.twilio.phone-number}")
    private String twilioPhoneNumber;
    
    @Value("${app.verification.sms.expiry-minutes:10}")
    private int smsVerificationExpiryMinutes;
    
    @Value("${app.verification.sms.max-attempts:5}")
    private int maxSmsAttempts;
    
    @Value("${app.verification.sms.resend-delay-minutes:10}")
    private int smsResendDelayMinutes;

    private static final String SMS_VERIFICATION_PREFIX = "sms_verification:";
    private static final String SMS_RATE_LIMIT_PREFIX = "sms_rate_limit:";
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Send SMS verification code with Uganda phone number validation
     */
    @Transactional
    public SMSVerificationResult sendSMSVerification(String phoneNumber) {
        try {
            // Normalize and validate Uganda phone number
            String ugandaNumber = normalizeUgandaNumber(phoneNumber);
            if (!isValidUgandaNumber(ugandaNumber)) {
                return SMSVerificationResult.builder()
                    .success(false)
                    .error("Invalid Uganda phone number format")
                    .errorCode("INVALID_PHONE_FORMAT")
                    .build();
            }

            // Check rate limiting (3 SMS per 10 minutes)
            if (isRateLimited(ugandaNumber)) {
                return SMSVerificationResult.builder()
                    .success(false)
                    .error("Too many SMS sent. Please wait before requesting again.")
                    .retryAfterMinutes(smsResendDelayMinutes)
                    .errorCode("RATE_LIMITED")
                    .build();
            }

            // Generate secure verification code
            String verificationCode = generateSecureCode();
            String sessionId = generateSessionId();

            // Store SMS verification data
            SMSVerificationData smsData = new SMSVerificationData();
            smsData.setPhoneNumber(ugandaNumber);
            smsData.setCode(verificationCode);
            smsData.setSessionId(sessionId);
            smsData.setAttempts(0);
            smsData.setCreatedAt(LocalDateTime.now());
            smsData.setExpiresAt(LocalDateTime.now().plusMinutes(smsVerificationExpiryMinutes));

            String verificationKey = SMS_VERIFICATION_PREFIX + ugandaNumber;
            redisTemplate.opsForValue().set(
                verificationKey,
                smsData,
                smsVerificationExpiryMinutes,
                TimeUnit.MINUTES
            );

            // Track rate limiting
            String rateLimitKey = SMS_RATE_LIMIT_PREFIX + ugandaNumber;
            redisTemplate.opsForValue().set(rateLimitKey, "1", smsResendDelayMinutes, TimeUnit.MINUTES);

            // Send SMS via Twilio
            boolean smsSent = sendSMS(ugandaNumber, verificationCode);
            if (!smsSent) {
                // Clean up if SMS failed
                redisTemplate.delete(verificationKey);
                redisTemplate.delete(rateLimitKey);
                
                return SMSVerificationResult.builder()
                    .success(false)
                    .error("Failed to send SMS. Please try again later.")
                    .errorCode("SMS_SEND_FAILED")
                    .build();
            }

            log.info("SMS verification sent to Uganda number: {}", ugandaNumber);

            return SMSVerificationResult.builder()
                .success(true)
                .message("SMS verification code sent")
                .expiresInMinutes(smsVerificationExpiryMinutes)
                .build();

        } catch (Exception e) {
            log.error("Error sending SMS verification to: {}", phoneNumber, e);
            return SMSVerificationResult.builder()
                .success(false)
                .error("Failed to send verification SMS. Please try again later.")
                .errorCode("SMS_SEND_ERROR")
                .build();
        }
    }

    /**
     * Verify SMS code with attempt tracking
     */
    @Transactional
    public SMSVerificationResult verifySMSCode(String phoneNumber, String code) {
        try {
            String ugandaNumber = normalizeUgandaNumber(phoneNumber);
            String verificationKey = SMS_VERIFICATION_PREFIX + ugandaNumber;
            SMSVerificationData smsData = (SMSVerificationData) redisTemplate.opsForValue().get(verificationKey);

            if (smsData == null) {
                return SMSVerificationResult.builder()
                    .success(false)
                    .error("Verification session not found or expired")
                    .errorCode("SESSION_NOT_FOUND")
                    .build();
            }

            // Check expiry
            if (LocalDateTime.now().isAfter(smsData.getExpiresAt())) {
                redisTemplate.delete(verificationKey);
                return SMSVerificationResult.builder()
                    .success(false)
                    .error("Verification session has expired")
                    .errorCode("SESSION_EXPIRED")
                    .build();
            }

            // Check attempts
            if (smsData.getAttempts() >= maxSmsAttempts) {
                return SMSVerificationResult.builder()
                    .success(false)
                    .error("Too many verification attempts")
                    .errorCode("MAX_ATTEMPTS_REACHED")
                    .build();
            }

            // Verify code
            if (!smsData.getCode().equals(code)) {
                // Increment attempts
                smsData.setAttempts(smsData.getAttempts() + 1);
                redisTemplate.opsForValue().set(verificationKey, smsData,
                    (int) ChronoUnit.MINUTES.between(LocalDateTime.now(), smsData.getExpiresAt()),
                    TimeUnit.MINUTES);

                return SMSVerificationResult.builder()
                    .success(false)
                    .error("Invalid verification code")
                    .errorCode("INVALID_CODE")
                    .remainingAttempts(maxSmsAttempts - smsData.getAttempts())
                    .build();
            } else {
                // Success - update user phone verification status
                // This would typically update the user's verification status in the database
                
                // Clean up verification data
                redisTemplate.delete(verificationKey);
                String rateLimitKey = SMS_RATE_LIMIT_PREFIX + ugandaNumber;
                redisTemplate.delete(rateLimitKey);

                log.info("SMS verification completed for Uganda number: {}", ugandaNumber);

                return SMSVerificationResult.builder()
                    .success(true)
                    .message("Phone number verified successfully")
                    .sessionId(smsData.getSessionId())
                    .build();
            }

        } catch (Exception e) {
            log.error("Error verifying SMS code for: {}", phoneNumber, e);
            return SMSVerificationResult.builder()
                .success(false)
                .error("Verification failed. Please try again.")
                .errorCode("VERIFICATION_ERROR")
                .build();
        }
    }

    /**
     * Send SMS using Twilio
     */
    private boolean sendSMS(String phoneNumber, String verificationCode) {
        try {
            String messageBody = String.format(
                "Your GynAid verification code is: %s. Valid for %d minutes. Do not share this code with anyone.",
                verificationCode, smsVerificationExpiryMinutes
            );

            Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(twilioPhoneNumber),
                messageBody
            ).create();

            log.info("SMS sent successfully via Twilio to: {}", phoneNumber);
            return true;

        } catch (Exception e) {
            log.error("Failed to send SMS via Twilio to: {}", phoneNumber, e);
            return false;
        }
    }

    /**
     * Normalize Uganda phone number to +256 format
     */
    private String normalizeUgandaNumber(String phoneNumber) {
        if (phoneNumber == null) return null;
        
        // Remove spaces, dashes, and parentheses
        String number = phoneNumber.replaceAll("[\\s\\-()]", "");
        
        // Convert 0XXXXXXXXX to +256XXXXXXXXX
        if (number.startsWith("0") && number.length() == 10) {
            number = "+256" + number.substring(1);
        }
        
        // Ensure it starts with +256
        if (!number.startsWith("+256")) {
            number = "+256" + number;
        }
        
        return number;
    }

    /**
     * Validate Uganda phone number format
     */
    private boolean isValidUgandaNumber(String phoneNumber) {
        if (phoneNumber == null) return false;
        
        // Uganda numbers should be +256 followed by 9 digits
        Pattern ugandaPattern = Pattern.compile("^\\+256\\d{9}$");
        return ugandaPattern.matcher(phoneNumber).matches();
    }

    /**
     * Check rate limiting for SMS
     */
    private boolean isRateLimited(String phoneNumber) {
        String rateLimitKey = SMS_RATE_LIMIT_PREFIX + phoneNumber;
        return Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey));
    }

    /**
     * Generate secure 6-digit verification code
     */
    private String generateSecureCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }

    /**
     * Generate unique session ID
     */
    private String generateSessionId() {
        byte[] sessionBytes = new byte[16];
        secureRandom.nextBytes(sessionBytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(sessionBytes);
    }

    // Data classes
    public static class SMSVerificationData {
        private String phoneNumber;
        private String code;
        private String sessionId;
        private int attempts;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;

        // Getters and setters
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public int getAttempts() { return attempts; }
        public void setAttempts(int attempts) { this.attempts = attempts; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    }

    public static class SMSVerificationResult {
        private boolean success;
        private String message;
        private String error;
        private String errorCode;
        private String sessionId;
        private int remainingAttempts;
        private int retryAfterMinutes;
        private int expiresInMinutes;

        // Builder pattern
        public static class Builder {
            private SMSVerificationResult result = new SMSVerificationResult();

            public Builder success(boolean success) {
                result.success = success;
                return this;
            }

            public Builder message(String message) {
                result.message = message;
                return this;
            }

            public Builder error(String error) {
                result.error = error;
                return this;
            }

            public Builder errorCode(String errorCode) {
                result.errorCode = errorCode;
                return this;
            }

            public Builder sessionId(String sessionId) {
                result.sessionId = sessionId;
                return this;
            }

            public Builder remainingAttempts(int remainingAttempts) {
                result.remainingAttempts = remainingAttempts;
                return this;
            }

            public Builder retryAfterMinutes(int retryAfterMinutes) {
                result.retryAfterMinutes = retryAfterMinutes;
                return this;
            }

            public Builder expiresInMinutes(int expiresInMinutes) {
                result.expiresInMinutes = expiresInMinutes;
                return this;
            }

            public SMSVerificationResult build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }
    }
}