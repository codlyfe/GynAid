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
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userService.userExists(request.getEmail())) {
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .message("User already exists with this email")
                    .build());
        }

        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole() != null ? request.getRole() : User.UserRole.CLIENT)
                .status(User.UserStatus.ACTIVE)
                .build();

        if (request.getLocation() != null) {
            Point point = LocationUtils.toPoint(
                request.getLocation().getLatitude(),
                request.getLocation().getLongitude()
            );

            ProviderLocation location = new ProviderLocation(
                null, // id
                user, // provider
                point, // currentLocation
                ProviderLocation.AvailabilityStatus.OFFLINE,
                null, // currentActivity
                LocalDateTime.now(), // lastUpdated
                null, // accuracy
                ProviderLocation.ServiceType.IMMEDIATE_CONSULTATION
            );

            user.setCurrentLocation(location);
        }

        var savedUser = userService.saveUser(user);
        var jwtToken = jwtService.generateToken(savedUser);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwtToken)
                .user(savedUser)
                .message("User registered successfully")
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
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
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(user);
    }
}
