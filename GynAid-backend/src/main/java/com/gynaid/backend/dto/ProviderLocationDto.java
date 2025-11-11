package com.gynaid.backend.dto;

import com.gynaid.backend.entity.ProviderLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderLocationDto {
    private Long id;
    private Long providerId;
    private String providerName;
    private String providerEmail;
    private Double latitude;
    private Double longitude;
    private ProviderLocation.AvailabilityStatus availabilityStatus;
    private String currentActivity;
    private LocalDateTime lastUpdated;
    private Double accuracy;
    private ProviderLocation.ServiceType serviceType;

    public static ProviderLocationDto fromEntity(ProviderLocation location) {
        if (location == null) {
            return null;
        }

        Double latitude = null;
        Double longitude = null;
        if (location.getCurrentLocation() != null) {
            latitude = location.getCurrentLocation().getY();
            longitude = location.getCurrentLocation().getX();
        }

        return ProviderLocationDto.builder()
            .id(location.getId())
            .providerId(location.getProvider() != null ? location.getProvider().getId() : null)
            .providerName(location.getProvider() != null ? 
                (location.getProvider().getFirstName() + " " + 
                 (location.getProvider().getLastName() != null ? location.getProvider().getLastName() : "")).trim() 
                : null)
            .providerEmail(location.getProvider() != null ? location.getProvider().getEmail() : null)
            .latitude(latitude)
            .longitude(longitude)
            .availabilityStatus(location.getAvailabilityStatus())
            .currentActivity(location.getCurrentActivity())
            .lastUpdated(location.getLastUpdated())
            .accuracy(location.getAccuracy())
            .serviceType(location.getServiceType())
            .build();
    }
}


