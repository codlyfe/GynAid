package com.gynaid.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Enterprise-grade Multi-Factor Authentication (TOTP) service for GynAid
 * Supports Google Authenticator and other TOTP-compatible apps
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MFAService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final QRCodeGenerator qrCodeGenerator;
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
    
    @Value("${app.mfa.backup-codes-count:10}")
    private int backupCodesCount;
    
    @Value("${app.mfa.backup-code-length:8}")
    private int backupCodeLength;

    private static final String MFA_SETUP_PREFIX = "mfa_setup:";
    private static final String MFA_USED_PREFIX = "mfa_used:";
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate MFA setup for user with QR code and backup codes
     */
    @Transactional
    public MFASetupResult generateMFASetup(String userId) {
        try {
            // Generate secret key
            GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
            String secret = key.getKey();
            
            // Generate QR code
            String otpauthUrl = "otpauth://totp/GynAid:" + userId + "?secret=" + key.getKey() + "&issuer=GynAid";
            String qrCodeDataUrl = qrCodeGenerator.generateQRCode(otpauthUrl);
            
            // Generate backup codes
            List<String> backupCodes = generateBackupCodes();
            
            // Store MFA setup data
            MFASetupData mfaSetup = new MFASetupData();
            mfaSetup.setSecret(secret);
            mfaSetup.setBackupCodes(backupCodes);
            mfaSetup.setQrCode(qrCodeDataUrl);
            mfaSetup.setOtpauthUrl(otpauthUrl);
            mfaSetup.setEnabled(false);
            mfaSetup.setCreatedAt(LocalDateTime.now());
            
            String setupKey = MFA_SETUP_PREFIX + userId;
            redisTemplate.opsForValue().set(setupKey, mfaSetup, 24, TimeUnit.HOURS); // 24 hours setup window

            log.info("MFA setup generated for user: {}", userId);

            return MFASetupResult.builder()
                .success(true)
                .secret(secret)
                .qrCode(qrCodeDataUrl)
                .backupCodes(backupCodes)
                .otpauthUrl(otpauthUrl)
                .build();

        } catch (Exception e) {
            log.error("Error generating MFA setup for user: {}", userId, e);
            return MFASetupResult.builder()
                .success(false)
                .error("Failed to generate MFA setup")
                .build();
        }
    }

    /**
     * Enable MFA for user after verification
     */
    @Transactional
    public MFAEnableResult enableMFA(String userId, String secret, String verificationCode) {
        try {
            String setupKey = MFA_SETUP_PREFIX + userId;
            MFASetupData mfaSetup = (MFASetupData) redisTemplate.opsForValue().get(setupKey);
            
            if (mfaSetup == null) {
                return MFAEnableResult.builder()
                    .success(false)
                    .error("MFA setup not found or expired")
                    .errorCode("SETUP_NOT_FOUND")
                    .build();
            }
            
            if (!mfaSetup.getSecret().equals(secret)) {
                return MFAEnableResult.builder()
                    .success(false)
                    .error("Invalid secret key")
                    .errorCode("INVALID_SECRET")
                    .build();
            }
            
            // Verify the TOTP code
            boolean isValid = googleAuthenticator.authorize(secret, Integer.parseInt(verificationCode));
            
            if (!isValid) {
                return MFAEnableResult.builder()
                    .success(false)
                    .error("Invalid verification code")
                    .errorCode("INVALID_CODE")
                    .build();
            }
            
            // Enable MFA
            mfaSetup.setEnabled(true);
            mfaSetup.setEnabledAt(LocalDateTime.now());
            redisTemplate.opsForValue().set(setupKey, mfaSetup, 30, TimeUnit.DAYS); // 30 days retention
            
            log.info("MFA enabled for user: {}", userId);

            return MFAEnableResult.builder()
                .success(true)
                .message("Multi-Factor Authentication enabled successfully")
                .build();

        } catch (Exception e) {
            log.error("Error enabling MFA for user: {}", userId, e);
            return MFAEnableResult.builder()
                .success(false)
                .error("Failed to enable MFA")
                .errorCode("ENABLE_ERROR")
                .build();
        }
    }

    /**
     * Verify TOTP code during login
     */
    @Transactional
    public MFAVerificationResult verifyTOTP(String userId, String code) {
        try {
            String setupKey = MFA_SETUP_PREFIX + userId;
            MFASetupData mfaSetup = (MFASetupData) redisTemplate.opsForValue().get(setupKey);
            
            if (mfaSetup == null || !mfaSetup.isEnabled()) {
                return MFAVerificationResult.builder()
                    .success(false)
                    .error("MFA not enabled for this user")
                    .errorCode("MFA_NOT_ENABLED")
                    .build();
            }
            
            // Check backup codes first
            if (isBackupCode(code, mfaSetup.getBackupCodes())) {
                // Mark backup code as used
                markBackupCodeAsUsed(userId, code);
                
                log.info("MFA backup code used for user: {}", userId);
                
                return MFAVerificationResult.builder()
                    .success(true)
                    .method("backup_code")
                    .message("Backup code verified successfully")
                    .build();
            }
            
            // Verify TOTP code
            boolean isValid = googleAuthenticator.authorize(mfaSetup.getSecret(), Integer.parseInt(code));
            
            if (isValid) {
                log.info("MFA TOTP verification successful for user: {}", userId);
                
                return MFAVerificationResult.builder()
                    .success(true)
                    .method("totp")
                    .message("TOTP verification successful")
                    .build();
            } else {
                return MFAVerificationResult.builder()
                    .success(false)
                    .error("Invalid TOTP code")
                    .errorCode("INVALID_TOTP_CODE")
                    .build();
            }
            
        } catch (NumberFormatException e) {
            return MFAVerificationResult.builder()
                .success(false)
                .error("Invalid code format")
                .errorCode("INVALID_FORMAT")
                .build();
        } catch (Exception e) {
            log.error("Error verifying MFA for user: {}", userId, e);
            return MFAVerificationResult.builder()
                .success(false)
                .error("MFA verification failed")
                .errorCode("VERIFICATION_ERROR")
                .build();
        }
    }

    /**
     * Disable MFA for user
     */
    @Transactional
    public MFADisableResult disableMFA(String userId, String verificationCode) {
        try {
            // First verify the user wants to disable MFA
            MFAVerificationResult verification = verifyTOTP(userId, verificationCode);
            if (!verification.success) {
                return MFADisableResult.builder()
                    .success(false)
                    .error("Invalid verification code")
                    .errorCode("VERIFICATION_FAILED")
                    .build();
            }
            
            // Disable MFA
            String setupKey = MFA_SETUP_PREFIX + userId;
            MFASetupData mfaSetup = (MFASetupData) redisTemplate.opsForValue().get(setupKey);
            
            if (mfaSetup != null) {
                mfaSetup.setEnabled(false);
                mfaSetup.setDisabledAt(LocalDateTime.now());
                redisTemplate.opsForValue().set(setupKey, mfaSetup, 30, TimeUnit.DAYS);
            }
            
            log.info("MFA disabled for user: {}", userId);

            return MFADisableResult.builder()
                .success(true)
                .message("Multi-Factor Authentication disabled successfully")
                .build();

        } catch (Exception e) {
            log.error("Error disabling MFA for user: {}", userId, e);
            return MFADisableResult.builder()
                .success(false)
                .error("Failed to disable MFA")
                .errorCode("DISABLE_ERROR")
                .build();
        }
    }

    /**
     * Check if MFA is enabled for user
     */
    public boolean isMFAEnabled(String userId) {
        try {
            String setupKey = MFA_SETUP_PREFIX + userId;
            MFASetupData mfaSetup = (MFASetupData) redisTemplate.opsForValue().get(setupKey);
            return mfaSetup != null && mfaSetup.isEnabled();
        } catch (Exception e) {
            log.error("Error checking MFA status for user: {}", userId, e);
            return false;
        }
    }

    /**
     * Generate secret key for TOTP
     */
    private String generateSecretKey() {
        byte[] secretBytes = new byte[32];
        secureRandom.nextBytes(secretBytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);
    }

    /**
     * Generate backup codes for MFA
     */
    private List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < backupCodesCount; i++) {
            StringBuilder code = new StringBuilder();
            for (int j = 0; j < backupCodeLength; j++) {
                code.append(secureRandom.nextInt(36)); // 0-9 and A-Z
                if (code.length() == 4) {
                    code.append('-'); // Add hyphen in the middle
                }
            }
            codes.add(code.toString().toUpperCase());
        }
        return codes;
    }

    /**
     * Check if code is a backup code
     */
    private boolean isBackupCode(String code, List<String> backupCodes) {
        return backupCodes.stream()
            .map(String::toUpperCase)
            .anyMatch(bc -> bc.equals(code.toUpperCase()));
    }

    /**
     * Mark backup code as used
     */
    private void markBackupCodeAsUsed(String userId, String code) {
        String usedKey = MFA_USED_PREFIX + userId + ":" + code.toUpperCase();
        redisTemplate.opsForValue().set(usedKey, "used", 365, TimeUnit.DAYS); // Keep for a year
        
        // Also update the main MFA setup to remove the used code
        String setupKey = MFA_SETUP_PREFIX + userId;
        MFASetupData mfaSetup = (MFASetupData) redisTemplate.opsForValue().get(setupKey);
        if (mfaSetup != null) {
            mfaSetup.getBackupCodes().removeIf(bc -> bc.toUpperCase().equals(code.toUpperCase()));
            redisTemplate.opsForValue().set(setupKey, mfaSetup, 30, TimeUnit.DAYS);
        }
    }

    // Data classes and result classes
    public static class MFASetupData {
        private String secret;
        private List<String> backupCodes;
        private String qrCode;
        private String otpauthUrl;
        private boolean enabled;
        private LocalDateTime createdAt;
        private LocalDateTime enabledAt;
        private LocalDateTime disabledAt;

        // Getters and setters
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public List<String> getBackupCodes() { return backupCodes; }
        public void setBackupCodes(List<String> backupCodes) { this.backupCodes = backupCodes; }
        public String getQrCode() { return qrCode; }
        public void setQrCode(String qrCode) { this.qrCode = qrCode; }
        public String getOtpauthUrl() { return otpauthUrl; }
        public void setOtpauthUrl(String otpauthUrl) { this.otpauthUrl = otpauthUrl; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getEnabledAt() { return enabledAt; }
        public void setEnabledAt(LocalDateTime enabledAt) { this.enabledAt = enabledAt; }
        public LocalDateTime getDisabledAt() { return disabledAt; }
        public void setDisabledAt(LocalDateTime disabledAt) { this.disabledAt = disabledAt; }
    }

    // Result classes with builder pattern
    public static class MFASetupResult {
        private boolean success;
        private String secret;
        private String qrCode;
        private List<String> backupCodes;
        private String otpauthUrl;
        private String error;

        public static class Builder {
            private MFASetupResult result = new MFASetupResult();

            public Builder success(boolean success) {
                result.success = success;
                return this;
            }

            public Builder secret(String secret) {
                result.secret = secret;
                return this;
            }

            public Builder qrCode(String qrCode) {
                result.qrCode = qrCode;
                return this;
            }

            public Builder backupCodes(List<String> backupCodes) {
                result.backupCodes = backupCodes;
                return this;
            }

            public Builder otpauthUrl(String otpauthUrl) {
                result.otpauthUrl = otpauthUrl;
                return this;
            }

            public Builder error(String error) {
                result.error = error;
                return this;
            }

            public MFASetupResult build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }
    }

    public static class MFAEnableResult {
        private boolean success;
        private String message;
        private String error;
        private String errorCode;

        public static class Builder {
            private MFAEnableResult result = new MFAEnableResult();

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

            public MFAEnableResult build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }
    }

    public static class MFAVerificationResult {
        private boolean success;
        private String method;
        private String message;
        private String error;
        private String errorCode;

        public boolean getSuccess() {
            return success;
        }

        public static class Builder {
            private MFAVerificationResult result = new MFAVerificationResult();

            public Builder success(boolean success) {
                result.success = success;
                return this;
            }

            public Builder method(String method) {
                result.method = method;
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

            public MFAVerificationResult build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }
    }

    public static class MFADisableResult {
        private boolean success;
        private String message;
        private String error;
        private String errorCode;

        public static class Builder {
            private MFADisableResult result = new MFADisableResult();

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

            public MFADisableResult build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }
    }
}