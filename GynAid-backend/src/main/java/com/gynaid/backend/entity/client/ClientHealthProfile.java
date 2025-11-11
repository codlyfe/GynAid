package com.gynaid.backend.entity.client;

import com.gynaid.backend.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_health_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientHealthProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "emergency_contact_name")
    private String emergencyContactName;
    
    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;
    
    @Column(name = "emergency_contact_relationship")
    private String emergencyContactRelationship;
    
    @Column(name = "profile_completion_percentage")
    @Builder.Default
    private Integer profileCompletionPercentage = 0;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @OneToOne(mappedBy = "healthProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private GynecologicalProfile gynecologicalProfile;
    
    @OneToOne(mappedBy = "healthProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MedicalVitals medicalVitals;
    
    @OneToOne(mappedBy = "healthProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MedicalHistory medicalHistory;
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
