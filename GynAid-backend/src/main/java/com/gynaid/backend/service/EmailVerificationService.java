package com.gynaid.backend.service;

import com.gynaid.backend.entity.User;
import com.gynaid.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Enterprise-grade email verification service
 * Implements secure, rate-limited email verification with retry protection
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;

    @Value("${app.verification.email.expiry-minutes:30}")
    private int emailVerificationExpiryMinutes;

    @Value("${app.verification.email.max-attempts:3}")
    private int maxVerificationAttempts;

    @Value("${app.verification.email.resend-delay-minutes:5}")
    private int resendDelayMinutes;

    private static final String EMAIL_VERIFICATION_PREFIX = "email_verification:";
    private static final String EMAIL_RATE_LIMIT_PREFIX = "email_rate_limit:";
    private static final SecureRandom secureRandom = new SecureRandom();

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Send email verification code with rate limiting
     */
    @Transactional
    public VerificationResult sendVerificationEmail(String email) {
        try {
            // Validate email format
            if (!isValidEmail(email)) {
                return VerificationResult.builder()
                    .success(false)
                    .error("Invalid email address format")
                    .build();
            }

            // Check rate limiting
            if (isRateLimited(email)) {
                return VerificationResult.builder()
                    .success(false)
                    .error("Too many verification attempts. Please wait before requesting again.")
                    .retryAfterMinutes(resendDelayMinutes)
                    .build();
            }

            // Check if user exists
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return VerificationResult.builder()
                    .success(false)
                    .error("No account found with this email address")
                    .build();
            }

            // Generate secure verification code
            String verificationCode = generateSecureCode();
            String verificationToken = generateSecureToken();

            // Store verification data in Redis with expiry
            String verificationKey = EMAIL_VERIFICATION_PREFIX + email;
            VerificationData verificationData = new VerificationData();
            verificationData.setEmail(email);
            verificationData.setCode(verificationCode);
            verificationData.setToken(verificationToken);
            verificationData.setUserId(user.getId());
            verificationData.setAttempts(0);
            verificationData.setCreatedAt(LocalDateTime.now());
            verificationData.setExpiresAt(LocalDateTime.now().plusMinutes(emailVerificationExpiryMinutes));

            redisTemplate.opsForValue().set(
                verificationKey,
                verificationData,
                emailVerificationExpiryMinutes,
                TimeUnit.MINUTES
            );

            // Track rate limiting
            String rateLimitKey = EMAIL_RATE_LIMIT_PREFIX + email;
            redisTemplate.opsForValue().set(rateLimitKey, "1", resendDelayMinutes, TimeUnit.MINUTES);

            // Send email
            sendVerificationEmail(user, verificationCode);

            log.info("Email verification sent to user: {} (ID: {})", email, user.getId());

            return VerificationResult.builder()
                .success(true)
                .message("Verification email sent successfully")
                .expiresInMinutes(emailVerificationExpiryMinutes)
                .build();

        } catch (Exception e) {
            log.error("Error sending email verification for: {}", email, e);
            return VerificationResult.builder()
                .success(false)
                .error("Failed to send verification email. Please try again later.")
                .build();
        }
    }

    /**
     * Verify email code with attempt tracking
     */
    @Transactional
    public VerificationResult verifyEmailCode(String email, String code) {
        try {
            String verificationKey = EMAIL_VERIFICATION_PREFIX + email;
            VerificationData verificationData = (VerificationData) redisTemplate.opsForValue().get(verificationKey);

            if (verificationData == null) {
                return VerificationResult.builder()
                    .success(false)
                    .error("Verification code not found or expired")
                    .errorCode("CODE_NOT_FOUND")
                    .build();
            }

            // Check if code is expired
            if (LocalDateTime.now().isAfter(verificationData.getExpiresAt())) {
                redisTemplate.delete(verificationKey);
                return VerificationResult.builder()
                    .success(false)
                    .error("Verification code has expired")
                    .errorCode("CODE_EXPIRED")
                    .build();
            }

            // Check attempt count
            if (verificationData.getAttempts() >= maxVerificationAttempts) {
                return VerificationResult.builder()
                    .success(false)
                    .error("Too many verification attempts")
                    .errorCode("MAX_ATTEMPTS_REACHED")
                    .build();
            }

            // Verify code
            if (!verificationData.getCode().equals(code)) {
                // Increment attempts
                verificationData.setAttempts(verificationData.getAttempts() + 1);
                redisTemplate.opsForValue().set(verificationKey, verificationData, 
                    (int) ChronoUnit.MINUTES.between(LocalDateTime.now(), verificationData.getExpiresAt()),
                    TimeUnit.MINUTES);

                return VerificationResult.builder()
                    .success(false)
                    .error("Invalid verification code")
                    .errorCode("INVALID_CODE")
                    .remainingAttempts(maxVerificationAttempts - verificationData.getAttempts())
                    .build();
            } else {
                // Success - update user verification status
                User user = userRepository.findById(verificationData.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

                user.setEmailVerified(true);
                user.setEmailVerifiedAt(LocalDateTime.now());
                userRepository.save(user);

                // Clean up verification data
                redisTemplate.delete(verificationKey);

                log.info("Email verification completed for user: {} (ID: {})", email, user.getId());

                return VerificationResult.builder()
                    .success(true)
                    .message("Email verified successfully")
                    .token(verificationData.getToken())
                    .build();
            }

        } catch (Exception e) {
            log.error("Error verifying email code for: {}", email, e);
            return VerificationResult.builder()
                .success(false)
                .error("Verification failed. Please try again.")
                .build();
        }
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
     * Generate secure verification token
     */
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Send verification email using template
     */
    private void sendVerificationEmail(User user, String verificationCode) {
        Context context = new Context();
        context.setVariable("userName", user.getFirstName() + " " + user.getLastName());
        context.setVariable("verificationCode", verificationCode);
        context.setVariable("expiryMinutes", emailVerificationExpiryMinutes);

        String emailContent = templateEngine.process("email/verification", context);
        
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setTo(user.getEmail());
        emailMessage.setSubject("Verify Your GynAid Email Address");
        emailMessage.setHtmlContent(emailContent);

        emailService.sendEmail(emailMessage);
    }

    /**
     * Validate email address format
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Check rate limiting for email verification
     */
    private boolean isRateLimited(String email) {
        String rateLimitKey = EMAIL_RATE_LIMIT_PREFIX + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey));
    }

    /**
     * Clean up expired verification data
     */
    @Transactional
    public void cleanupExpiredVerifications() {
        // Implementation for cleaning up expired verification data
        // This would be called by a scheduled task
    }

    // Data classes
    public static class VerificationData {
        private String email;
        private String code;
        private String token;
        private Long userId;
        private int attempts;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public int getAttempts() { return attempts; }
        public void setAttempts(int attempts) { this.attempts = attempts; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    }

    public static class VerificationResult {
        private boolean success;
        private String message;
        private String error;
        private String errorCode;
        private String token;
        private int remainingAttempts;
        private int retryAfterMinutes;
        private int expiresInMinutes;

        // Builder pattern
        public static class Builder {
            private VerificationResult result = new VerificationResult();

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

            public Builder token(String token) {
                result.token = token;
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

            public VerificationResult build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }
    }

    public static class EmailMessage {
        private String to;
        private String subject;
        private String htmlContent;

        // Getters and setters
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getHtmlContent() { return htmlContent; }
        public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }
    }
}