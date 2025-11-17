package com.gynaid.backend.repository;

import com.gynaid.backend.entity.Appointment;
import com.gynaid.backend.entity.AppointmentAuditTrail;
import com.gynaid.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for managing appointment audit trail entries
 */
@Repository
public interface AppointmentAuditTrailRepository extends JpaRepository<AppointmentAuditTrail, Long> {

    /**
     * Find all audit trail entries for a specific appointment
     */
    List<AppointmentAuditTrail> findByAppointment(Appointment appointment);

    /**
     * Find all audit trail entries for a specific appointment, ordered by creation date (newest first)
     */
    List<AppointmentAuditTrail> findByAppointmentOrderByCreatedAtDesc(Appointment appointment);

    /**
     * Find all audit trail entries for a specific user
     */
    List<AppointmentAuditTrail> findByUser(User user);

    /**
     * Find audit trail entries by action type
     */
    List<AppointmentAuditTrail> findByAction(String action);

    /**
     * Find audit trail entries by appointment and action type
     */
    List<AppointmentAuditTrail> findByAppointmentAndAction(Appointment appointment, String action);

    /**
     * Find audit trail entries created within a date range
     */
    List<AppointmentAuditTrail> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find audit trail entries for appointments within a date range
     */
    @Query("SELECT aat FROM AppointmentAuditTrail aat WHERE aat.appointment.startTime BETWEEN :startDate AND :endDate")
    List<AppointmentAuditTrail> findByAppointmentStartTimeBetween(@Param("startDate") LocalDateTime startDate, 
                                                                @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit trail entries by user and action type
     */
    List<AppointmentAuditTrail> findByUserAndAction(User user, String action);

    /**
     * Get count of audit trail entries for an appointment
     */
    @Query("SELECT COUNT(aat) FROM AppointmentAuditTrail aat WHERE aat.appointment = :appointment")
    long countByAppointment(@Param("appointment") Appointment appointment);

    /**
     * Find recent audit trail entries (last N entries)
     */
    List<AppointmentAuditTrail> findTop20ByOrderByCreatedAtDesc();

    /**
     * Find audit trail entries with specific status changes
     */
    @Query("SELECT aat FROM AppointmentAuditTrail aat WHERE aat.previousStatus IS NOT NULL OR aat.newStatus IS NOT NULL")
    List<AppointmentAuditTrail> findStatusChangeEntries();
}