package com.gynaid.backend.repository.client;

import com.gynaid.backend.entity.client.ClientHealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClientHealthProfileRepository extends JpaRepository<ClientHealthProfile, Long> {
    
    Optional<ClientHealthProfile> findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);
}
