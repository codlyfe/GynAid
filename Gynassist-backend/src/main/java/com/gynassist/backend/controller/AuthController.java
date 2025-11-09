package com.gynassist.backend.controller;

import com.gynassist.backend.dto.AuthRequest;
import com.gynassist.backend.dto.AuthResponse;
import com.gynassist.backend.dto.RegisterRequest;
import com.gynassist.backend.entity.ProviderLocation;
import com.gynassist.backend.entity.User;
import com.gynassist.backend.security.JwtService;
import com.gynassist.backend.service.UserService;
import com.gynassist.backend.util.LocationUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, 
                         JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {

            
            // Check if user already exists
            if (userService.userExists(request.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                        .message("User already exists with this email")
                        .build());
            }

            // Create user with validated data
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            
            User user = User.builder()
                .email(request.getEmail())
                .password(hashedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole() != null ? request.getRole() : User.UserRole.CLIENT)
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
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .message("Registration failed: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            var user = (User) authentication.getPrincipal();
            var jwtToken = jwtService.generateToken(user);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwtToken)
                    .user(user)
                    .message("Login successful")
                    .build());
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .message("Login failed: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(user);
    }
}
