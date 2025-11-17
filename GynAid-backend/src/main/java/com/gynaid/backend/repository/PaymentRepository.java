package com.gynaid.backend.repository;

import com.gynaid.backend.entity.Appointment;
import com.gynaid.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payments by appointment
     */
    List<Payment> findByAppointment(Appointment appointment);

    /**
     * Find payment by Stripe payment intent ID
     */
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    /**
     * Find payment by Stripe payment intent ID containing (for partial matching)
     */
    List<Payment> findByStripePaymentIntentIdContaining(String stripePaymentIntentId);

    /**
     * Find payment by idempotency key (for preventing duplicate payments)
     */
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find successful payments within date range
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'SUCCEEDED' AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findSuccessfulPaymentsBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Get payment statistics for admin dashboard
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCEEDED' AND p.createdAt >= :fromDate")
    BigDecimal getTotalRevenueSince(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'SUCCEEDED' AND p.createdAt >= :fromDate")
    long getSuccessfulPaymentCountSince(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.createdAt >= :fromDate")
    long getTotalPaymentCountSince(@Param("fromDate") LocalDateTime fromDate);

    /**
     * Find payments by status for analytics
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.createdAt >= :fromDate")
    List<Payment> findPaymentsByStatusSince(@Param("status") Payment.PaymentStatus status, 
                                           @Param("fromDate") LocalDateTime fromDate);

    /**
     * Get platform fee statistics
     */
    @Query("SELECT SUM(p.platformFee) FROM Payment p WHERE p.status = 'SUCCEEDED' AND p.createdAt >= :fromDate")
    BigDecimal getTotalPlatformFeesSince(@Param("fromDate") LocalDateTime fromDate);

    /**
     * Find payments for a specific provider's appointments
     */
    @Query("SELECT p FROM Payment p JOIN p.appointment a WHERE a.provider.id = :providerId")
    List<Payment> findPaymentsForProvider(@Param("providerId") Long providerId);

    /**
     * Find failed payments for retry logic
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.createdAt >= :fromDate ORDER BY p.createdAt DESC")
    List<Payment> findFailedPaymentsForRetry(@Param("fromDate") LocalDateTime fromDate);
}