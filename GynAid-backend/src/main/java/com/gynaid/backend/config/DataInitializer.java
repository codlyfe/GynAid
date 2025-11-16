package com.gynaid.backend.config;

import com.gynaid.backend.entity.User;
import com.gynaid.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default test user if not exists
        if (!userService.userExists("test@GynAid.com")) {
            User testUser = User.builder()
                .email("test@GynAid.com")
                .password(passwordEncoder.encode("TestPassword123!"))
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+256700000000")
                .role(User.UserRole.CLIENT)
                .status(User.UserStatus.ACTIVE)
                .profileCompletionStatus(User.ProfileCompletionStatus.BASIC_COMPLETE)
                .build();
            
            userService.saveUser(testUser);
            System.out.println("Created test user: test@GynAid.com / TestPassword123!");
        }
    }
}
