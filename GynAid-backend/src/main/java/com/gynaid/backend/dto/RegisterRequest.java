package com.gynaid.backend.dto;

import com.gynaid.backend.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

/**
 * Enhanced DTO for user registration requests.
 * Includes validation, builder support, and optional profile fields.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    // Required fields
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    // Optional fields - Enhanced from TypeScript utilities
    private LocalDate dateOfBirth;
    private String physicalAddress;
    private String preferredLanguage; // Default: "en"
    
    // Enum from User entity
    private User.UserRole role; // Default: CLIENT

    // DTO version of Location to decouple persistence logic
    private LocationDto location;
}

