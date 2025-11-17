package com.gynaid.backend.repository;

import com.gynaid.backend.entity.Appointment;
import com.gynaid.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Find appointments by client with pagination
     */
    Page<Appointment> findByClient(User client, Pageable pageable);

    /**
     * Find appointments by client and status
     */
    Page<Appointment> findByClientAndStatus(User client, Appointment.AppointmentStatus status, Pageable pageable);

    /**
     * Find appointments by provider with pagination
     */
    Page<Appointment> findByProvider(User provider, Pageable pageable);

    /**
     * Find appointments by provider and status
     */
    Page<Appointment> findByProviderAndStatus(User provider, Appointment.AppointmentStatus status, Pageable pageable);

    /**
     * Find appointments by status with pagination
     */
    Page<Appointment> findByStatus(Appointment.AppointmentStatus status, Pageable pageable);

    /**
     * Find appointments within date range
     */
    Page<Appointment> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find appointments by status and date range
     */
    Page<Appointment> findByStatusAndStartTimeBetween(Appointment.AppointmentStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Get appointment statistics for admin dashboard
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.createdAt >= :fromDate")
    long getTotalAppointmentsSince(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'COMPLETED' AND a.createdAt >= :fromDate")
    long getCompletedAppointmentsSince(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'CANCELLED' AND a.createdAt >= :fromDate")
    long getCancelledAppointmentsSince(@Param("fromDate") LocalDateTime fromDate);

    /**
     * Find appointments for a specific date (for scheduling)
     */
    @Query("SELECT a FROM Appointment a WHERE DATE(a.startTime) = DATE(:date)")
    List<Appointment> findByDate(@Param("date") LocalDateTime date);

    /**
     * Find upcoming appointments for a provider
     */
    @Query("SELECT a FROM Appointment a WHERE a.provider = :provider AND a.startTime > :now ORDER BY a.startTime ASC")
    List<Appointment> findUpcomingAppointmentsForProvider(@Param("provider") User provider, @Param("now") LocalDateTime now);

    /**
     * Find appointments by payment status
     */
    @Query("SELECT a FROM Appointment a WHERE a.paymentStatus = :paymentStatus")
    List<Appointment> findByPaymentStatus(Appointment.PaymentStatus paymentStatus);

    /**
     * Get provider performance metrics
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.provider = :provider AND a.status = 'COMPLETED' AND a.createdAt >= :fromDate")
    long getCompletedAppointmentsForProvider(@Param("provider") User provider, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT AVG(SIZE(a.payments)) FROM Appointment a WHERE a.provider = :provider AND a.status = 'COMPLETED' AND a.createdAt >= :fromDate")
    Double getAveragePaymentsPerAppointment(@Param("provider") User provider, @Param("fromDate") LocalDateTime fromDate);
}