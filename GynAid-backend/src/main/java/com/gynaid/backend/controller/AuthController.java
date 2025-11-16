package com.gynaid.backend.controller;

import com.gynaid.backend.dto.AuthRequest;
import com.gynaid.backend.dto.AuthResponse;
import com.gynaid.backend.dto.LocationDto;
import com.gynaid.backend.dto.RegisterRequest;
import com.gynaid.backend.entity.ProviderLocation;
import com.gynaid.backend.entity.User;
import com.gynaid.backend.security.JwtService;
import com.gynaid.backend.service.InputValidationService;
import com.gynaid.backend.service.RateLimitingService;
import com.gynaid.backend.service.UserService;
import com.gynaid.backend.util.LocationUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final InputValidationService inputValidationService;
    private final RateLimitingService rateLimitingService;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder, InputValidationService inputValidationService, RateLimitingService rateLimitingService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.inputValidationService = inputValidationService;
        this.rateLimitingService = rateLimitingService;
    }

    /**
     * Mask email address for secure logging
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 5) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***@***";
        }
        return email.substring(0, 1) + "***" + email.substring(atIndex - 1);
    }

    /**
     * Get client IP address for rate limiting
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            long startTime = System.currentTimeMillis();
            
            // 🔒 RATE LIMITING: Check registration rate limit
            String clientIp = getClientIpAddress(httpRequest);
            var registerRateLimit = rateLimitingService.checkRegisterRateLimit(clientIp, request.getEmail());
            
            if (!registerRateLimit.isAllowed()) {
                log.warn("Registration rate limit exceeded - IP: {}, Email: {}, Attempts: {}, Reset time: {}",
                    clientIp, maskEmail(request.getEmail()), registerRateLimit.getCurrentCount(), registerRateLimit.getResetTime());
                
                return ResponseEntity.status(429)
                    .body(AuthResponse.builder()
                        .message("Too many registration attempts. Please try again later.")
                        .errors(List.of("Rate limit exceeded. Try again in " + registerRateLimit.getRemainingTimeSeconds() + " seconds."))
                        .build());
            }
            
            // SECURITY CRITICAL: Use the new input validation service
            InputValidationService.RegistrationInput validationInput = InputValidationService.RegistrationInput.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole() != null ? request.getRole().name() : null)
                .build();
            
            var validationResult = inputValidationService.validateRegistrationInput(validationInput);
            
            if (!validationResult.isValid()) {
                // Log failed registration attempt with email masking
                log.warn("Registration validation failed - Email: {}, Errors: {}",
                    maskEmail(request.getEmail()), validationResult.getErrors());
                
                return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                        .message("Invalid input data")
                        .errors(validationResult.getErrors())
                        .build());
            }
            
            // Check if user already exists using sanitized email
            InputValidationService.RegistrationInput sanitizedData =
                (InputValidationService.RegistrationInput) validationResult.getSanitizedData();
            if (userService.userExists(sanitizedData.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                        .message("User already exists with this email")
                        .build());
            }

            // Create user with validated and sanitized data
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            
            // Convert role enum to string if provided
            User.UserRole userRole = User.UserRole.CLIENT; // Default role
            if (request.getRole() != null) {
                userRole = request.getRole();
            }
            
            User user = User.builder()
                .email(sanitizedData.getEmail())
                .password(hashedPassword)
                .firstName(sanitizedData.getFirstName())
                .lastName(sanitizedData.getLastName())
                .phoneNumber(sanitizedData.getPhoneNumber())
                .role(userRole)
                .status(User.UserStatus.ACTIVE)
                .profileCompletionStatus(User.ProfileCompletionStatus.BASIC_COMPLETE)
                .build();

            // Handle location if provided
            if (request.getLocation() != null) {
                Point point = LocationUtils.toPoint(
                    request.getLocation().getLatitude(),
                    request.getLocation().getLongitude()
                );

                ProviderLocation location = new ProviderLocation();
                location.setProvider(user);
                location.setCurrentLocation(point);
                location.setAvailabilityStatus(ProviderLocation.AvailabilityStatus.OFFLINE);
                location.setLastUpdated(LocalDateTime.now());
                location.setServiceType(ProviderLocation.ServiceType.IMMEDIATE_CONSULTATION);

                user.setCurrentLocation(location);
            }

            var savedUser = userService.saveUser(user);
            var jwtToken = jwtService.generateToken(savedUser);

            String message = "User registered successfully";

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwtToken)
                    .user(savedUser)
                    .message(message)
                    .build());
                    
        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .message("Registration failed: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        try {
            long startTime = System.currentTimeMillis();
            
            // SECURITY CRITICAL: Use the new input validation service
            InputValidationService.LoginInput loginInput = InputValidationService.LoginInput.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
            
            var validationResult = inputValidationService.validateLoginInput(loginInput);
            
            if (!validationResult.isValid()) {
                // Log failed login attempt with user ID masking
                log.warn("Login validation failed - Email: {}, Errors: {}", 
                    maskEmail(request.getEmail()), validationResult.getErrors());
                
                return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                        .message("Invalid input data")
                        .errors(validationResult.getErrors())
                        .build());
            }
            
            // SECURE AUTHENTICATION: Use Spring Security AuthenticationManager
            // This validates against the actual user database
            // Cast sanitized data to correct type
            Object sanitizedData = validationResult.getSanitizedData();
            String email = sanitizedData instanceof com.gynaid.backend.service.InputValidationService.LoginInput
                ? ((com.gynaid.backend.service.InputValidationService.LoginInput) sanitizedData).getEmail()
                : request.getEmail();
                
            var authenticationToken = new UsernamePasswordAuthenticationToken(
                email,
                request.getPassword()
            );
            
            // Authenticate with real credentials
            var authenticatedUser = authenticationManager.authenticate(authenticationToken);
            
            // Get the actual user from database
            var actualUser = (User) authenticatedUser.getPrincipal();
            
            // Generate JWT token for the real user
            var jwtToken = jwtService.generateToken(actualUser);
            
            // Log successful authentication
            long duration = System.currentTimeMillis() - startTime;
            log.info("Successful authentication - UserId: {}, Email: {}, Duration: {}ms", 
                actualUser.getId(), actualUser.getEmail(), duration);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwtToken)
                    .user(actualUser)
                    .message("Login successful")
                    .build());
                    
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // Security logging for failed authentication attempts
            log.warn("Failed login attempt - Email: {}, Reason: Invalid credentials", 
                maskEmail(request.getEmail()));
            
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .message("Invalid email or password")
                    .build());
                    
        } catch (org.springframework.security.authentication.LockedException e) {
            log.warn("Account locked - Email: {}", maskEmail(request.getEmail()));
            
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .message("Account is locked. Please contact support.")
                    .build());
                    
        } catch (Exception e) {
            log.error("Authentication error - Email: {}, Error: {}", 
                maskEmail(request.getEmail()), e.getMessage(), e);
            
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .message("Authentication failed. Please try again.")
                    .build());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Backend is working!");
    }
}
