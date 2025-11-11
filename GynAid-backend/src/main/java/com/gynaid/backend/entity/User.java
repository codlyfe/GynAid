package com.gynaid.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.gynaid.backend.entity.client.ClientHealthProfile;
import com.gynaid.backend.entity.provider.ProviderPracticeInfo;
import com.gynaid.backend.entity.provider.ProviderVerification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phoneNumber;
    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status;

    @OneToOne(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ProviderLocation currentLocation;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MOHLicense> licenses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InsurancePolicy> insurancePolicies;

    // ========== NEW FIELDS - All nullable for backward compatibility ==========
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "physical_address", columnDefinition = "TEXT")
    private String physicalAddress;

    @Column(name = "preferred_language")
    @Builder.Default
    private String preferredLanguage = "en"; // Default to English

    @Column(name = "profile_completion_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProfileCompletionStatus profileCompletionStatus = ProfileCompletionStatus.NOT_STARTED;

    // New relationships - all optional (nullable)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ClientHealthProfile healthProfile;

    @OneToOne(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ProviderVerification providerVerification;

    @OneToOne(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ProviderPracticeInfo practiceInfo;

    // ========== End of new fields ==========

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
        public boolean isEnabled() {
            return status == UserStatus.ACTIVE;
        }
        
        // Explicit implementation of UserDetails method
        @Override
        public String getPassword() {
            return password;
        }
    
        public enum UserRole {
            CLIENT, PROVIDER_INDIVIDUAL, PROVIDER_INSTITUTION, ADMIN
        }

    public enum UserStatus {
        ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION
    }

    public enum ProfileCompletionStatus {
        NOT_STARTED, IN_PROGRESS, BASIC_COMPLETE, FULLY_COMPLETE
    }
}

