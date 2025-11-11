package com.gynaid.backend.repository;

import com.gynaid.backend.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {

    // Optional: Add custom query methods if needed
    // Example: List<Provider> findBySpecialty(String specialty);
}

