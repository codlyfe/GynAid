package com.gynaid.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Enterprise-grade input validation and sanitization service
 *
 * Provides comprehensive security validation for all user inputs
 * to prevent XSS, injection attacks, and other security vulnerabilities.
 *
 * @author GynAid Security Team
 * @version 1.0
 */
@Slf4j
@Service
public class InputValidationService {

    // Security patterns for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+256[0-9]{9}$"
    );
    
    private static final Pattern SAFE_STRING_PATTERN = Pattern.compile(
        "^[A-Za-z0-9\\s\\-_.,!?()@]+$"
    );
    
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$"
    );
    
    // Dangerous patterns to detect and block
    private static final Pattern SCRIPT_TAG_PATTERN = Pattern.compile(
        "(?i)<script[^>]*>.*?</script>"
    );
    
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile(
        "(?i)javascript:"
    );
    
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(\\b(union|select|insert|update|delete|drop|create|alter|exec|execute)\\b)"
    );
    
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)(<iframe|<object|<embed|<link|<style|<meta|<img|<svg|<script)"
    );
    
    // Content length limits
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_PHONE_LENGTH = 13;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
    private static final int MAX_PASSWORD_LENGTH = 128;
    
    /**
     * Validates and sanitizes registration input data
     */
    public ValidationResult validateRegistrationInput(RegistrationInput input) {
        List<String> errors = new ArrayList<>();
        
        try {
            // Validate email
            String emailValidation = validateEmail(input.getEmail());
            if (emailValidation != null) {
                errors.add("email: " + emailValidation);
            }
            
            // Validate password strength
            String passwordValidation = validatePassword(input.getPassword());
            if (passwordValidation != null) {
                errors.add("password: " + passwordValidation);
            }
            
            // Validate names
            String firstNameValidation = validateName(input.getFirstName(), "First name");
            if (firstNameValidation != null) {
                errors.add("firstName: " + firstNameValidation);
            }
            
            String lastNameValidation = validateName(input.getLastName(), "Last name");
            if (lastNameValidation != null) {
                errors.add("lastName: " + lastNameValidation);
            }
            
            // Validate phone number
            String phoneValidation = validatePhoneNumber(input.getPhoneNumber());
            if (phoneValidation != null) {
                errors.add("phoneNumber: " + phoneValidation);
            }
            
            // Sanitize all string inputs
            RegistrationInput sanitizedInput = sanitizeRegistrationInput(input);
            
            boolean isValid = errors.isEmpty();
            
            log.info("Registration validation - Valid: {}, Errors: {}", isValid, errors.size());
            if (!errors.isEmpty()) {
                log.warn("Registration validation failed - Details: {}", errors);
            }
            
            return ValidationResult.builder()
                .isValid(isValid)
                .errors(errors)
                .sanitizedData(sanitizedInput)
                .build();
                
        } catch (Exception e) {
            log.error("Error during registration validation", e);
            errors.add("system: Validation error occurred");
            
            return ValidationResult.builder()
                .isValid(false)
                .errors(errors)
                .sanitizedData(input)
                .build();
        }
    }
    
    /**
     * Validates login input
     */
    public ValidationResult validateLoginInput(LoginInput input) {
        List<String> errors = new ArrayList<>();
        
        try {
            // Validate email
            String emailValidation = validateEmail(input.getEmail());
            if (emailValidation != null) {
                errors.add("email: " + emailValidation);
            }
            
            // Validate password presence
            if (!StringUtils.hasText(input.getPassword())) {
                errors.add("password: Password is required");
            }
            
            // Sanitize inputs
            LoginInput sanitizedInput = sanitizeLoginInput(input);
            
            boolean isValid = errors.isEmpty();
            
            log.info("Login validation - Valid: {}, Errors: {}", isValid, errors.size());
            
            return ValidationResult.builder()
                .isValid(isValid)
                .errors(errors)
                .sanitizedData(sanitizedInput)
                .build();
                
        } catch (Exception e) {
            log.error("Error during login validation", e);
            errors.add("system: Validation error occurred");
            
            return ValidationResult.builder()
                .isValid(false)
                .errors(errors)
                .sanitizedData(input)
                .build();
        }
    }
    
    /**
     * Validates email address
     */
    public String validateEmail(String email) {
        if (email == null) {
            return "Email is required";
        }
        
        String trimmed = email.trim();
        
        if (trimmed.isEmpty()) {
            return "Email is required";
        }
        
        if (trimmed.length() > MAX_EMAIL_LENGTH) {
            return "Email address is too long";
        }
        
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            return "Invalid email address format";
        }
        
        // Check for potentially dangerous characters
        if (trimmed.contains("<") || trimmed.contains(">") || trimmed.contains(";")) {
            return "Email contains invalid characters";
        }
        
        return null;
    }
    
    /**
     * Validates password strength
     */
    public String validatePassword(String password) {
        if (password == null) {
            return "Password is required";
        }
        
        if (password.length() < 12) {
            return "Password must be at least 12 characters long";
        }
        
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return "Password is too long";
        }
        
        // Check for strong password requirements
        if (!STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
            return "Password must contain uppercase, lowercase, number, and special character";
        }
        
        // Check for common weak passwords
        List<String> weakPasswords = List.of(
            "password", "123456", "qwerty", "admin", "letmein",
            "welcome", "monkey", "dragon", "master", "hello"
        );
        
        String lowerPassword = password.toLowerCase();
        for (String weak : weakPasswords) {
            if (lowerPassword.contains(weak)) {
                return "Password is too common or weak";
            }
        }
        
        return null;
    }
    
    /**
     * Validates name field
     */
    public String validateName(String name, String fieldName) {
        if (name == null) {
            return fieldName + " is required";
        }
        
        String trimmed = name.trim();
        
        if (trimmed.isEmpty()) {
            return fieldName + " is required";
        }
        
        if (trimmed.length() > MAX_NAME_LENGTH) {
            return fieldName + " is too long";
        }
        
        if (!SAFE_STRING_PATTERN.matcher(trimmed).matches()) {
            return fieldName + " contains invalid characters";
        }
        
        return null;
    }
    
    /**
     * Validates phone number (Uganda format)
     */
    public String validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return "Phone number is required";
        }
        
        String cleaned = phoneNumber.replaceAll("[\\s-()]", "");
        
        if (cleaned.isEmpty()) {
            return "Phone number is required";
        }
        
        if (cleaned.length() > MAX_PHONE_LENGTH) {
            return "Phone number is too long";
        }
        
        // Handle different formats for Uganda numbers
        String ugandaNumber = normalizeUgandaNumber(cleaned);
        
        if (!PHONE_PATTERN.matcher(ugandaNumber).matches()) {
            return "Invalid Uganda phone number format (use +256XXXXXXXXX)";
        }
        
        return null;
    }
    
    /**
     * Sanitizes user input to prevent XSS and injection attacks
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        String sanitized = input;
        
        // Remove script tags
        sanitized = SCRIPT_TAG_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove javascript: protocols
        sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove potentially dangerous HTML tags
        sanitized = XSS_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove SQL injection patterns
        sanitized = SQL_INJECTION_PATTERN.matcher(sanitized).replaceAll("");
        
        // Trim and normalize whitespace
        sanitized = sanitized.trim().replaceAll("\\s+", " ");
        
        return sanitized;
    }
    
    /**
     * Sanitizes registration input data
     */
    private RegistrationInput sanitizeRegistrationInput(RegistrationInput input) {
        return RegistrationInput.builder()
            .email(sanitizeInput(input.getEmail()))
            .password(input.getPassword()) // Don't sanitize password, just validate
            .firstName(sanitizeInput(input.getFirstName()))
            .lastName(sanitizeInput(input.getLastName()))
            .phoneNumber(normalizeUgandaNumber(input.getPhoneNumber()))
            .role(input.getRole())
            .build();
    }
    
    /**
     * Sanitizes login input data
     */
    private LoginInput sanitizeLoginInput(LoginInput input) {
        return LoginInput.builder()
            .email(sanitizeInput(input.getEmail()))
            .password(input.getPassword()) // Don't sanitize password
            .build();
    }
    
    /**
     * Normalizes Uganda phone number to standard format
     */
    private String normalizeUgandaNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        
        String cleaned = phoneNumber.replaceAll("[\\s-()]", "");
        
        // Handle 0XXXXXXXXX format
        if (cleaned.startsWith("0") && cleaned.length() == 10) {
            return "+256" + cleaned.substring(1);
        }
        
        // Handle 256XXXXXXXXX format
        if (cleaned.startsWith("256") && cleaned.length() == 12) {
            return "+" + cleaned;
        }
        
        // Already in correct format
        if (cleaned.startsWith("+256") && cleaned.length() == 13) {
            return cleaned;
        }
        
        return phoneNumber; // Return original if can't normalize
    }
    
    /**
     * Validates health data input
     */
    public ValidationResult validateHealthData(HealthDataInput input) {
        List<String> errors = new ArrayList<>();
        
        try {
            // Validate and sanitize medical history
            if (input.getMedicalHistory() != null) {
                String medicalHistoryValidation = validateDescription(input.getMedicalHistory());
                if (medicalHistoryValidation != null) {
                    errors.add("medicalHistory: " + medicalHistoryValidation);
                }
            }
            
            // Validate and sanitize symptoms
            if (input.getSymptoms() != null) {
                String symptomsValidation = validateDescription(input.getSymptoms());
                if (symptomsValidation != null) {
                    errors.add("symptoms: " + symptomsValidation);
                }
            }
            
            // Validate numeric values
            if (input.getHeight() != null && (input.getHeight() < 50 || input.getHeight() > 300)) {
                errors.add("height: Must be between 50 and 300 cm");
            }
            
            if (input.getWeight() != null && (input.getWeight() < 20 || input.getWeight() > 500)) {
                errors.add("weight: Must be between 20 and 500 kg");
            }
            
            // Sanitize all string inputs
            HealthDataInput sanitizedInput = HealthDataInput.builder()
                .medicalHistory(sanitizeInput(input.getMedicalHistory()))
                .symptoms(sanitizeInput(input.getSymptoms()))
                .height(input.getHeight())
                .weight(input.getWeight())
                .bloodType(input.getBloodType())
                .build();
            
            boolean isValid = errors.isEmpty();
            
            return ValidationResult.builder()
                .isValid(isValid)
                .errors(errors)
                .sanitizedData(sanitizedInput)
                .build();
                
        } catch (Exception e) {
            log.error("Error during health data validation", e);
            errors.add("system: Validation error occurred");
            
            return ValidationResult.builder()
                .isValid(false)
                .errors(errors)
                .sanitizedData(input)
                .build();
        }
    }
    
    /**
     * Validates description fields (medical history, symptoms, etc.)
     */
    private String validateDescription(String description) {
        if (description == null) {
            return null; // Optional field
        }
        
        String trimmed = description.trim();
        
        if (trimmed.isEmpty()) {
            return null; // Empty is allowed for optional fields
        }
        
        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            return "Description is too long (maximum " + MAX_DESCRIPTION_LENGTH + " characters)";
        }
        
        // Check for dangerous patterns
        if (SCRIPT_TAG_PATTERN.matcher(trimmed).find() ||
            JAVASCRIPT_PATTERN.matcher(trimmed).find() ||
            XSS_PATTERN.matcher(trimmed).find()) {
            return "Description contains potentially dangerous content";
        }
        
        return null;
    }
    
    // Data transfer objects for validation
    
    public static class RegistrationInput {
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String role;
        
        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public static RegistrationInputBuilder builder() {
            return new RegistrationInputBuilder();
        }
        
        public static class RegistrationInputBuilder {
            private RegistrationInput input = new RegistrationInput();
            
            public RegistrationInputBuilder email(String email) {
                input.setEmail(email);
                return this;
            }
            
            public RegistrationInputBuilder password(String password) {
                input.setPassword(password);
                return this;
            }
            
            public RegistrationInputBuilder firstName(String firstName) {
                input.setFirstName(firstName);
                return this;
            }
            
            public RegistrationInputBuilder lastName(String lastName) {
                input.setLastName(lastName);
                return this;
            }
            
            public RegistrationInputBuilder phoneNumber(String phoneNumber) {
                input.setPhoneNumber(phoneNumber);
                return this;
            }
            
            public RegistrationInputBuilder role(String role) {
                input.setRole(role);
                return this;
            }
            
            public RegistrationInput build() {
                return input;
            }
        }
    }
    
    public static class LoginInput {
        private String email;
        private String password;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public static LoginInputBuilder builder() {
            return new LoginInputBuilder();
        }
        
        public static class LoginInputBuilder {
            private LoginInput input = new LoginInput();
            
            public LoginInputBuilder email(String email) {
                input.setEmail(email);
                return this;
            }
            
            public LoginInputBuilder password(String password) {
                input.setPassword(password);
                return this;
            }
            
            public LoginInput build() {
                return input;
            }
        }
    }
    
    public static class HealthDataInput {
        private String medicalHistory;
        private String symptoms;
        private Double height;
        private Double weight;
        private String bloodType;
        
        public String getMedicalHistory() { return medicalHistory; }
        public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }
        
        public String getSymptoms() { return symptoms; }
        public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
        
        public Double getHeight() { return height; }
        public void setHeight(Double height) { this.height = height; }
        
        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }
        
        public String getBloodType() { return bloodType; }
        public void setBloodType(String bloodType) { this.bloodType = bloodType; }
        
        public static HealthDataInputBuilder builder() {
            return new HealthDataInputBuilder();
        }
        
        public static class HealthDataInputBuilder {
            private HealthDataInput input = new HealthDataInput();
            
            public HealthDataInputBuilder medicalHistory(String medicalHistory) {
                input.setMedicalHistory(medicalHistory);
                return this;
            }
            
            public HealthDataInputBuilder symptoms(String symptoms) {
                input.setSymptoms(symptoms);
                return this;
            }
            
            public HealthDataInputBuilder height(Double height) {
                input.setHeight(height);
                return this;
            }
            
            public HealthDataInputBuilder weight(Double weight) {
                input.setWeight(weight);
                return this;
            }
            
            public HealthDataInputBuilder bloodType(String bloodType) {
                input.setBloodType(bloodType);
                return this;
            }
            
            public HealthDataInput build() {
                return input;
            }
        }
    }
    
    public static class ValidationResult {
        private boolean isValid;
        private List<String> errors;
        private Object sanitizedData;
        
        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public Object getSanitizedData() { return sanitizedData; }
        public void setSanitizedData(Object sanitizedData) { this.sanitizedData = sanitizedData; }
        
        public static ValidationResultBuilder builder() {
            return new ValidationResultBuilder();
        }
        
        public static class ValidationResultBuilder {
            private ValidationResult result = new ValidationResult();
            
            public ValidationResultBuilder isValid(boolean isValid) {
                result.setValid(isValid);
                return this;
            }
            
            public ValidationResultBuilder errors(List<String> errors) {
                result.setErrors(errors);
                return this;
            }
            
            public ValidationResultBuilder sanitizedData(Object sanitizedData) {
                result.setSanitizedData(sanitizedData);
                return this;
            }
            
            public ValidationResult build() {
                return result;
            }
        }
    }
}