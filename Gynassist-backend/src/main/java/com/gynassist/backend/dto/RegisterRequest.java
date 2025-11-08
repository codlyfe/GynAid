package com.gynassist.backend.dto;

import com.gynassist.backend.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for user registration requests.
 * Includes validation and builder support.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;
    private String phoneNumber;

    // Enum from User entity
    private User.UserRole role;

    // DTO version of Location to decouple persistence logic
    private LocationDto location;
}
