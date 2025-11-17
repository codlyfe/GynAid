package com.gynaid.backend.repository;

import com.gynaid.backend.entity.HealthScore;
import com.gynaid.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthScoreRepository extends JpaRepository<HealthScore, Long> {

    /**
     * Find the most recent health score for a user
     */
    Optional<HealthScore> findTopByClientOrderByCreatedAtDesc(User client);

    /**
     * Find health score history for a user after a specific date
     */
    List<HealthScore> findByClientAndCreatedAtAfterOrderByCreatedAtDesc(User client, LocalDateTime fromDate);

    /**
     * Find health scores for a user within a date range
     */
    List<HealthScore> findByClientAndCreatedAtBetweenOrderByCreatedAtDesc(User client, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get health score statistics for admin dashboard
     */
    @Query("SELECT AVG(h.scoreValue) FROM HealthScore h WHERE h.createdAt >= :fromDate")
    Double getAverageHealthScoreSince(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT COUNT(h) FROM HealthScore h WHERE h.createdAt >= :fromDate")
    long getHealthScoreCountSince(@Param("fromDate") LocalDateTime fromDate);

    /**
     * Find users with concerning health scores
     */
    @Query("SELECT h.client FROM HealthScore h WHERE h.scoreValue < :threshold AND h.createdAt >= :fromDate")
    List<User> findUsersWithConcerningScores(@Param("threshold") BigDecimal threshold, @Param("fromDate") LocalDateTime fromDate);
}