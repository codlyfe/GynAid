package com.gynassist.backend.repository.client;

import com.gynassist.backend.entity.client.ClientHealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientHealthProfileRepository extends JpaRepository<ClientHealthProfile, Long> {
    Optional<ClientHealthProfile> findByUserId(Long userId);
}

