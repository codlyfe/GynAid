package com.gynassist.backend.repository;

import com.gynassist.backend.entity.ProviderSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderSubscriptionRepository extends JpaRepository<ProviderSubscription, Long> {

    Optional<ProviderSubscription> findByProviderId(Long providerId);
    
    Page<ProviderSubscription> findByStatus(ProviderSubscription.SubscriptionStatus status, Pageable pageable);
    
    List<ProviderSubscription> findByEndDateBeforeAndStatus(
        LocalDateTime endDate, 
        ProviderSubscription.SubscriptionStatus status);
    
    List<ProviderSubscription> findByLastPaymentDateBetween(
        LocalDateTime startDate, 
        LocalDateTime endDate);
    
    Integer countByStatus(ProviderSubscription.SubscriptionStatus status);
    
    @Query("SELECT COUNT(s) FROM ProviderSubscription s WHERE s.plan = :plan AND s.status = 'ACTIVE'")
    Integer countActiveByPlan(@Param("plan") ProviderSubscription.SubscriptionPlan plan);
    
    @Query("SELECT SUM(s.monthlyFee) FROM ProviderSubscription s WHERE s.status = 'ACTIVE'")
    Double getTotalMonthlyRecurringRevenue();
    
    @Query("SELECT s FROM ProviderSubscription s WHERE s.nextBillingDate <= :date AND s.status = 'ACTIVE'")
    List<ProviderSubscription> findDueForBilling(@Param("date") LocalDateTime date);
}