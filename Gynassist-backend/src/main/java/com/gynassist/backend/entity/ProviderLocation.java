package com.gynassist.backend.entity;

import java.time.LocalDateTime;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "provider_locations")
public class ProviderLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    /**
     * Spatial column for geolocation.
     * Ensure PostGIS is enabled and SRID 4326 is used for WGS84 coordinates.
     */
    @Column(name = "current_location", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point currentLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false)
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.OFFLINE;

    @Column(name = "current_activity")
    private String currentActivity;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "accuracy")
    private Double accuracy;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;

    public enum AvailabilityStatus {
        ONLINE,
        OFFLINE,
        BUSY,
        ON_BREAK
    }

    public enum ServiceType {
        IMMEDIATE_CONSULTATION,
        SCHEDULED_VISIT,
        HOME_VISIT,
        TELEMEDICINE
    }
}
