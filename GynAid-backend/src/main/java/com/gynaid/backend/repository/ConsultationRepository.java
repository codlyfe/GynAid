package com.gynaid.backend.repository;

import com.gynaid.backend.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    List<Consultation> findByClientIdOrderByCreatedAtDesc(Long clientId);
    
    List<Consultation> findByProviderIdOrderByScheduledDateTimeAsc(Long providerId);
    
    List<Consultation> findByStatusAndScheduledDateTimeBetween(
        Consultation.ConsultationStatus status,
        LocalDateTime start,
        LocalDateTime end);
    
    @Query("SELECT SUM(c.appFee) FROM Consultation c WHERE c.paymentStatus = 'COMPLETED' AND c.paymentDateTime BETWEEN :start AND :end")
    Double getTotalAppFeeRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    Integer countByPaymentStatus(Consultation.PaymentStatus status);
}
