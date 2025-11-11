package com.gynaid.backend.service;

import com.gynaid.backend.entity.ProviderLocation;
import com.gynaid.backend.entity.User;
import com.gynaid.backend.repository.ProviderLocationRepository;
import com.gynaid.backend.repository.UserRepository;
import com.gynaid.backend.util.LocationUtils;
import jakarta.persistence.EntityNotFoundException;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LocationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProviderLocationRepository providerLocationRepository;

    /**
     * Updates the latitude and longitude of a provider by User ID.
     * Creates a ProviderLocation if it doesn't exist, or updates the existing one.
     *
     * @param userId   the ID of the user (provider)
     * @param latitude the new latitude
     * @param longitude the new longitude
     */
    @Transactional
    public void updateProviderLocation(Long userId, Double latitude, Double longitude) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // Check if user is a provider
        if (user.getRole() != User.UserRole.PROVIDER_INDIVIDUAL && 
            user.getRole() != User.UserRole.PROVIDER_INSTITUTION) {
            throw new IllegalArgumentException("User with ID " + userId + " is not a provider");
        }

        // Get or create ProviderLocation
        ProviderLocation location = providerLocationRepository.findByProviderId(userId)
            .orElseGet(() -> {
                ProviderLocation newLocation = new ProviderLocation();
                newLocation.setProvider(user);
                newLocation.setAvailabilityStatus(ProviderLocation.AvailabilityStatus.OFFLINE);
                newLocation.setServiceType(ProviderLocation.ServiceType.IMMEDIATE_CONSULTATION);
                return newLocation;
            });

        // Update location
        Point point = LocationUtils.toPoint(latitude, longitude);
        location.setCurrentLocation(point);
        location.setLastUpdated(LocalDateTime.now());
        
        // If status was OFFLINE and we're updating location, set to ONLINE
        if (location.getAvailabilityStatus() == ProviderLocation.AvailabilityStatus.OFFLINE) {
            location.setAvailabilityStatus(ProviderLocation.AvailabilityStatus.ONLINE);
        }

        providerLocationRepository.save(location);
    }

    /**
     * Finds providers within a given radius (in kilometers) of the specified latitude and longitude.
     * Uses Haversine formula to calculate distance for H2 database compatibility.
     *
     * @param latitude  the latitude of the search center
     * @param longitude the longitude of the search center
     * @param radiusKm  the search radius in kilometers
     * @return a list of nearby ProviderLocation entities within the radius
     */
    public List<ProviderLocation> findNearbyProviders(Double latitude, Double longitude, Double radiusKm) {
        // Get all provider locations (in production, this could be optimized with spatial queries)
        List<ProviderLocation> allLocations = providerLocationRepository.findAll();
        
        // Filter by distance using Haversine formula
        return allLocations.stream()
            .filter(location -> {
                if (location.getCurrentLocation() == null) {
                    return false;
                }
                
                double providerLat = LocationUtils.getLatitude(location.getCurrentLocation());
                double providerLon = LocationUtils.getLongitude(location.getCurrentLocation());
                
                double distance = LocationUtils.calculateDistanceKm(
                    latitude, longitude, 
                    providerLat, providerLon
                );
                
                return distance <= radiusKm;
            })
            .collect(Collectors.toList());
    }

    /**
     * Finds providers within a given radius, optionally filtered by service type and availability.
     *
     * @param latitude  the latitude of the search center
     * @param longitude the longitude of the search center
     * @param radiusKm  the search radius in kilometers
     * @param serviceType optional service type filter
     * @param onlyAvailable if true, only returns providers with ONLINE or BUSY status
     * @return a list of nearby ProviderLocation entities
     */
    public List<ProviderLocation> findNearbyProviders(
            Double latitude, Double longitude, Double radiusKm,
            ProviderLocation.ServiceType serviceType, boolean onlyAvailable) {
        
        List<ProviderLocation> nearby = findNearbyProviders(latitude, longitude, radiusKm);
        
        // Filter by service type if provided
        if (serviceType != null) {
            nearby = nearby.stream()
                .filter(location -> location.getServiceType() == serviceType)
                .collect(Collectors.toList());
        }
        
        // Filter by availability if requested
        if (onlyAvailable) {
            nearby = nearby.stream()
                .filter(location -> 
                    location.getAvailabilityStatus() == ProviderLocation.AvailabilityStatus.ONLINE ||
                    location.getAvailabilityStatus() == ProviderLocation.AvailabilityStatus.BUSY
                )
                .collect(Collectors.toList());
        }
        
        return nearby;
    }

    /**
     * Retrieves all provider locations.
     *
     * @return a list of all ProviderLocation entities
     */
    public List<ProviderLocation> getAllProviderLocations() {
        return providerLocationRepository.findAll();
    }

    /**
     * Gets a provider location by user ID.
     *
     * @param userId the user ID
     * @return Optional ProviderLocation
     */
    public Optional<ProviderLocation> getProviderLocationByUserId(Long userId) {
        return providerLocationRepository.findByProviderId(userId);
    }

    /**
     * Updates the availability status of a provider.
     *
     * @param userId the user ID
     * @param status the new availability status
     */
    @Transactional
    public void updateAvailabilityStatus(Long userId, ProviderLocation.AvailabilityStatus status) {
        ProviderLocation location = providerLocationRepository.findByProviderId(userId)
            .orElseThrow(() -> new EntityNotFoundException("Provider location not found for user ID: " + userId));
        
        location.setAvailabilityStatus(status);
        location.setLastUpdated(LocalDateTime.now());
        providerLocationRepository.save(location);
    }
}

