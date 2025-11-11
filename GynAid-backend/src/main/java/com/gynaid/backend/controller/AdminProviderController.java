package com.gynaid.backend.controller;

import com.gynaid.backend.entity.HealthcareProvider;
import com.gynaid.backend.entity.ProviderSubscription;
import com.gynaid.backend.service.AdminProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/providers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminProviderController {

    private final AdminProviderService adminProviderService;

    // Provider Management
    @PostMapping
    public ResponseEntity<HealthcareProvider> createProvider(@RequestBody CreateProviderRequest request) {
        HealthcareProvider provider = adminProviderService.createProvider(request);
        return ResponseEntity.ok(provider);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HealthcareProvider> updateProvider(
            @PathVariable Long id, 
            @RequestBody UpdateProviderRequest request) {
        HealthcareProvider provider = adminProviderService.updateProvider(id, request);
        return ResponseEntity.ok(provider);
    }

    @GetMapping
    public ResponseEntity<Page<HealthcareProvider>> getAllProviders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) HealthcareProvider.VerificationStatus status) {
        Page<HealthcareProvider> providers = adminProviderService.getAllProviders(page, size, status);
        return ResponseEntity.ok(providers);
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<HealthcareProvider> verifyProvider(@PathVariable Long id) {
        HealthcareProvider provider = adminProviderService.verifyProvider(id);
        return ResponseEntity.ok(provider);
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<HealthcareProvider> suspendProvider(@PathVariable Long id) {
        HealthcareProvider provider = adminProviderService.suspendProvider(id);
        return ResponseEntity.ok(provider);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(@PathVariable Long id) {
        adminProviderService.deleteProvider(id);
        return ResponseEntity.ok().build();
    }

    // Subscription Management
    @PostMapping("/{providerId}/subscription")
    public ResponseEntity<ProviderSubscription> createSubscription(
            @PathVariable Long providerId,
            @RequestBody CreateSubscriptionRequest request) {
        ProviderSubscription subscription = adminProviderService.createSubscription(providerId, request);
        return ResponseEntity.ok(subscription);
    }

    @PutMapping("/{providerId}/subscription")
    public ResponseEntity<ProviderSubscription> updateSubscription(
            @PathVariable Long providerId,
            @RequestBody UpdateSubscriptionRequest request) {
        ProviderSubscription subscription = adminProviderService.updateSubscription(providerId, request);
        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<Page<ProviderSubscription>> getAllSubscriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ProviderSubscription.SubscriptionStatus status) {
        Page<ProviderSubscription> subscriptions = adminProviderService.getAllSubscriptions(page, size, status);
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/subscriptions/expiring")
    public ResponseEntity<List<ProviderSubscription>> getExpiringSubscriptions(
            @RequestParam(defaultValue = "7") int daysAhead) {
        List<ProviderSubscription> expiring = adminProviderService.getExpiringSubscriptions(daysAhead);
        return ResponseEntity.ok(expiring);
    }

    // Analytics & Reports
    @GetMapping("/analytics/revenue")
    public ResponseEntity<RevenueReport> getRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        RevenueReport report = adminProviderService.getRevenueReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/analytics/providers")
    public ResponseEntity<ProviderAnalytics> getProviderAnalytics() {
        ProviderAnalytics analytics = adminProviderService.getProviderAnalytics();
        return ResponseEntity.ok(analytics);
    }

    // Payment Processing
    @PostMapping("/{providerId}/payment")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable Long providerId,
            @RequestBody PaymentRequest request) {
        PaymentResponse response = adminProviderService.processPayment(providerId, request);
        return ResponseEntity.ok(response);
    }

    // DTOs
    @lombok.Data
    public static class CreateProviderRequest {
        private String name;
        private String email;
        private String phoneNumber;
        private HealthcareProvider.ProviderType type;
        private List<HealthcareProvider.Specialization> specializations;
        private String address;
        private String district;
        private String region;
        private String country;
        private String description;
        private String website;
        private String licenseNumber;
        private Integer experienceYears;
        private Double consultationFee;
        private List<String> languages;
        private List<String> services;
        private ProviderSubscription.SubscriptionPlan initialPlan;
    }

    @lombok.Data
    public static class UpdateProviderRequest {
        private String name;
        private String email;
        private String phoneNumber;
        private String description;
        private String website;
        private Double consultationFee;
        private List<String> services;
        private HealthcareProvider.AvailabilityStatus availabilityStatus;
    }

    @lombok.Data
    public static class CreateSubscriptionRequest {
        private ProviderSubscription.SubscriptionPlan plan;
        private Integer durationMonths;
        private Boolean autoRenew;
    }

    @lombok.Data
    public static class UpdateSubscriptionRequest {
        private ProviderSubscription.SubscriptionPlan plan;
        private ProviderSubscription.SubscriptionStatus status;
        private Boolean autoRenew;
    }

    @lombok.Data
    public static class PaymentRequest {
        private String paymentMethod;
        private String transactionId;
        private Double amount;
        private String currency;
    }

    @lombok.Data
    @lombok.Builder
    public static class PaymentResponse {
        private boolean success;
        private String message;
        private String receiptId;
        private ProviderSubscription.SubscriptionStatus newStatus;
    }

    @lombok.Data
    @lombok.Builder
    public static class RevenueReport {
        private Double totalRevenue;
        private Double monthlyRecurring;
        private Integer activeSubscriptions;
        private Integer newSubscriptions;
        private Integer cancelledSubscriptions;
        private List<PlanRevenue> revenueByPlan;
    }

    @lombok.Data
    @lombok.Builder
    public static class PlanRevenue {
        private ProviderSubscription.SubscriptionPlan plan;
        private Double revenue;
        private Integer subscribers;
    }

    @lombok.Data
    @lombok.Builder
    public static class ProviderAnalytics {
        private Integer totalProviders;
        private Integer verifiedProviders;
        private Integer pendingVerification;
        private Integer suspendedProviders;
        private Integer activeSubscriptions;
        private Double averageRating;
        private List<SpecializationCount> topSpecializations;
    }

    @lombok.Data
    @lombok.Builder
    public static class SpecializationCount {
        private HealthcareProvider.Specialization specialization;
        private Integer count;
    }
}
