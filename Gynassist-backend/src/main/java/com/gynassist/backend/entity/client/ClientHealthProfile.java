package com.gynassist.backend.entity.client;

import com.gynassist.backend.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Comprehensive health profile for clients.
 * Linked to User via OneToOne relationship.
 * All fields are nullable to maintain backward compatibility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "client_health_profiles")
public class ClientHealthProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Profile completion tracking
    @Column(name = "completion_percentage")
    @Builder.Default
    private Integer completionPercentage = 0;

    @Column(name = "last_updated_profile")
    private LocalDateTime lastUpdatedProfile;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships to other profile entities
    @OneToOne(mappedBy = "healthProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private MedicalVitals medicalVitals;

    @OneToOne(mappedBy = "healthProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private MedicalHistory medicalHistory;

    @OneToOne(mappedBy = "healthProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private GynecologicalProfile gynecologicalProfile;
}

