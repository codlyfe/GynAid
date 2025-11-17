package com.gynaid.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.gynaid.backend.entity.provider.*;

@Entity
@Table(name = "providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @NotNull
    @Column(name = "name")
    private String name;

    @Column(name = "specialty")
    private String specialty;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // Optional: contact info, rating, availability, etc.
    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "is_active")
    private Boolean isActive;

    // Add relationships if needed
    // @OneToMany(mappedBy = "provider")
    // private List<Appointment> appointments;

    // Helper methods to match what the controller expects
    public User getUser() { return user; }
    
    public String getPhoneNumber() {
        return user != null && user.getPhoneNumber() != null ?
            user.getPhoneNumber() : phone;
    }
    
    public User.UserRole getRole() {
        return user != null ? user.getRole() : null;
    }
    
    public String getFirstName() {
        return user != null ? user.getFirstName() : name;
    }
    
    public String getLastName() {
        return user != null ? user.getLastName() : "";
    }
    
    public String getEmail() {
        return user != null ? user.getEmail() : email;
    }
    
    public ProviderLocation getCurrentLocation() {
        return user != null ? user.getCurrentLocation() : null;
    }
    
    public ProviderPracticeInfo getPracticeInfo() {
        return user != null ? user.getPracticeInfo() : null;
    }
    
    public ProviderVerification getProviderVerification() {
        return user != null ? user.getProviderVerification() : null;
    }
}

