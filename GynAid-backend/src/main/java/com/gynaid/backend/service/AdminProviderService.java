package com.gynaid.backend.service;

import com.gynaid.backend.controller.AdminProviderController.*;
import com.gynaid.backend.entity.HealthcareProvider;
import com.gynaid.backend.entity.ProviderSubscription;
import com.gynaid.backend.repository.HealthcareProviderRepository;
import com.gynaid.backend.repository.ProviderSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminProviderService {

    private final HealthcareProviderRepository providerRepository;
    private final ProviderSubscriptionRepository subscriptionRepository;

    // Provider Management
    @Transactional
    public HealthcareProvider createProvider(CreateProviderRequest request) {
        HealthcareProvider provider = HealthcareProvider.builder()
            .name(request.getName())
            .email(request.getEmail())
            .phoneNumber(request.getPhoneNumber())
            .type(request.getType())
            .scope(HealthcareProvider.GeographicScope.UGANDA) // Default to Uganda
            .specializations(request.getSpecializations())
            .address(request.getAddress())
            .district(request.getDistrict())
            .region(request.getRegion())
            .country(request.getCountry())
            .description(request.getDescription())
            .website(request.getWebsite())
            .licenseNumber(request.getLicenseNumber())
            .experienceYears(request.getExperienceYears())
            .verificationStatus(HealthcareProvider.VerificationStatus.PENDING)
            .availabilityStatus(HealthcareProvider.AvailabilityStatus.OFFLINE)
            .rating(0.0)
            .reviewCount(0)
            .consultationFee(request.getConsultationFee())
            .languages(request.getLanguages())
            .services(request.getServices())
            .build();

        HealthcareProvider savedProvider = providerRepository.save(provider);

        // Create initial subscription if specified
        if (request.getInitialPlan() != null) {
            createInitialSubscription(savedProvider, request.getInitialPlan());
        }

        return savedProvider;
    }

    @Transactional
    public HealthcareProvider updateProvider(Long id, UpdateProviderRequest request) {
        HealthcareProvider provider = providerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Provider not found"));

        if (request.getName() != null) provider.setName(request.getName());
        if (request.getEmail() != null) provider.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) provider.setPhoneNumber(request.getPhoneNumber());
        if (request.getDescription() != null) provider.setDescription(request.getDescription());
        if (request.getWebsite() != null) provider.setWebsite(request.getWebsite());
        if (request.getConsultationFee() != null) provider.setConsultationFee(request.getConsultationFee());
        if (request.getServices() != null) provider.setServices(request.getServices());
        if (request.getAvailabilityStatus() != null) provider.setAvailabilityStatus(request.getAvailabilityStatus());

        return providerRepository.save(provider);
    }

    public Page<HealthcareProvider> getAllProviders(int page, int size, HealthcareProvider.VerificationStatus status) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null) {
            return providerRepository.findByVerificationStatus(status, pageable);
        }
        return providerRepository.findAll(pageable);
    }

    @Transactional
    public HealthcareProvider verifyProvider(Long id) {
        HealthcareProvider provider = providerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Provider not found"));
        
        provider.setVerificationStatus(HealthcareProvider.VerificationStatus.VERIFIED);
        provider.setAvailabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE);
        
        return providerRepository.save(provider);
    }

    @Transactional
    public HealthcareProvider suspendProvider(Long id) {
        HealthcareProvider provider = providerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Provider not found"));
        
        provider.setVerificationStatus(HealthcareProvider.VerificationStatus.SUSPENDED);
        provider.setAvailabilityStatus(HealthcareProvider.AvailabilityStatus.OFFLINE);
        
        return providerRepository.save(provider);
    }

    @Transactional
    public void deleteProvider(Long id) {
        providerRepository.deleteById(id);
    }

    // Subscription Management
    @Transactional
    public ProviderSubscription createSubscription(Long providerId, CreateSubscriptionRequest request) {
        HealthcareProvider provider = providerRepository.findById(providerId)
            .orElseThrow(() -> new RuntimeException("Provider not found"));

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMonths(request.getDurationMonths());

        ProviderSubscription subscription = ProviderSubscription.builder()
            .provider(provider)
            .plan(request.getPlan())
            .status(ProviderSubscription.SubscriptionStatus.PENDING_PAYMENT)
            .startDate(startDate)
            .endDate(endDate)
            .monthlyFee(request.getPlan().getMonthlyFee())
            .totalPaid(BigDecimal.ZERO)
            .nextBillingDate(startDate.plusMonths(1))
            .priorityRanking(request.getPlan().getPriorityRanking())
            .featuredListing(request.getPlan().getFeaturedListing())
            .premiumBadge(request.getPlan().getPremiumBadge())
            .maxPhotos(request.getPlan().getMaxPhotos())
            .videoConsultationEnabled(request.getPlan().getVideoConsultationEnabled())
            .emergencyCallsEnabled(request.getPlan().getEmergencyCallsEnabled())
            .analyticsAccess(request.getPlan().getAnalyticsAccess())
            .customBranding(request.getPlan().getCustomBranding())
            .build();

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public ProviderSubscription updateSubscription(Long providerId, UpdateSubscriptionRequest request) {
        ProviderSubscription subscription = subscriptionRepository.findByProviderId(providerId)
            .orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (request.getPlan() != null && !request.getPlan().equals(subscription.getPlan())) {
            // Plan change - update features and pricing
            subscription.setPlan(request.getPlan());
            subscription.setMonthlyFee(request.getPlan().getMonthlyFee());
            subscription.setPriorityRanking(request.getPlan().getPriorityRanking());
            subscription.setFeaturedListing(request.getPlan().getFeaturedListing());
            subscription.setPremiumBadge(request.getPlan().getPremiumBadge());
            subscription.setMaxPhotos(request.getPlan().getMaxPhotos());
            subscription.setVideoConsultationEnabled(request.getPlan().getVideoConsultationEnabled());
            subscription.setEmergencyCallsEnabled(request.getPlan().getEmergencyCallsEnabled());
            subscription.setAnalyticsAccess(request.getPlan().getAnalyticsAccess());
            subscription.setCustomBranding(request.getPlan().getCustomBranding());
        }

        if (request.getStatus() != null) {
            subscription.setStatus(request.getStatus());
        }

        return subscriptionRepository.save(subscription);
    }

    public Page<ProviderSubscription> getAllSubscriptions(int page, int size, ProviderSubscription.SubscriptionStatus status) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null) {
            return subscriptionRepository.findByStatus(status, pageable);
        }
        return subscriptionRepository.findAll(pageable);
    }

    public List<ProviderSubscription> getExpiringSubscriptions(int daysAhead) {
        LocalDateTime cutoffDate = LocalDateTime.now().plusDays(daysAhead);
        return subscriptionRepository.findByEndDateBeforeAndStatus(cutoffDate, ProviderSubscription.SubscriptionStatus.ACTIVE);
    }

    // Payment Processing
    @Transactional
    public PaymentResponse processPayment(Long providerId, PaymentRequest request) {
        ProviderSubscription subscription = subscriptionRepository.findByProviderId(providerId)
            .orElseThrow(() -> new RuntimeException("Subscription not found"));

        try {
            // Simulate payment processing
            boolean paymentSuccess = processPaymentWithGateway(request);
            
            if (paymentSuccess) {
                subscription.setStatus(ProviderSubscription.SubscriptionStatus.ACTIVE);
                subscription.setLastPaymentDate(LocalDateTime.now());
                subscription.setTotalPaid(subscription.getTotalPaid().add(BigDecimal.valueOf(request.getAmount())));
                subscription.setNextBillingDate(LocalDateTime.now().plusMonths(1));
                
                subscriptionRepository.save(subscription);
                
                return PaymentResponse.builder()
                    .success(true)
                    .message("Payment processed successfully")
                    .receiptId(UUID.randomUUID().toString())
                    .newStatus(ProviderSubscription.SubscriptionStatus.ACTIVE)
                    .build();
            } else {
                return PaymentResponse.builder()
                    .success(false)
                    .message("Payment failed")
                    .newStatus(subscription.getStatus())
                    .build();
            }
        } catch (Exception e) {
            return PaymentResponse.builder()
                .success(false)
                .message("Payment processing error: " + e.getMessage())
                .newStatus(subscription.getStatus())
                .build();
        }
    }

    // Analytics & Reports
    public RevenueReport getRevenueReport(String startDate, String endDate) {
        LocalDateTime start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE).atTime(23, 59, 59);
        
        List<ProviderSubscription> subscriptions = subscriptionRepository.findByLastPaymentDateBetween(start, end);
        
        Double totalRevenue = subscriptions.stream()
            .mapToDouble(s -> s.getTotalPaid().doubleValue())
            .sum();
            
        Double monthlyRecurring = subscriptionRepository.findByStatus(ProviderSubscription.SubscriptionStatus.ACTIVE, PageRequest.of(0, Integer.MAX_VALUE))
            .getContent().stream()
            .mapToDouble(s -> s.getMonthlyFee().doubleValue())
            .sum();

        return RevenueReport.builder()
            .totalRevenue(totalRevenue)
            .monthlyRecurring(monthlyRecurring)
            .activeSubscriptions(subscriptionRepository.countByStatus(ProviderSubscription.SubscriptionStatus.ACTIVE))
            .build();
    }

    public ProviderAnalytics getProviderAnalytics() {
        return ProviderAnalytics.builder()
            .totalProviders(Math.toIntExact(providerRepository.count()))
            .verifiedProviders(providerRepository.countByVerificationStatus(HealthcareProvider.VerificationStatus.VERIFIED))
            .pendingVerification(providerRepository.countByVerificationStatus(HealthcareProvider.VerificationStatus.PENDING))
            .suspendedProviders(providerRepository.countByVerificationStatus(HealthcareProvider.VerificationStatus.SUSPENDED))
            .activeSubscriptions(subscriptionRepository.countByStatus(ProviderSubscription.SubscriptionStatus.ACTIVE))
            .build();
    }

    // Helper Methods
    private void createInitialSubscription(HealthcareProvider provider, ProviderSubscription.SubscriptionPlan plan) {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlan(plan);
        request.setDurationMonths(1);
        request.setAutoRenew(false);
        
        createSubscription(provider.getId(), request);
    }

    private boolean processPaymentWithGateway(PaymentRequest request) {
        // Simulate payment gateway integration
        // In production, integrate with actual payment processors like Stripe, MTN Mobile Money, etc.
        return true; // Simulate successful payment
    }
}
