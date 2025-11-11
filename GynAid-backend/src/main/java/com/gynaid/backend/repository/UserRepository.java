package com.gynaid.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gynaid.backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Checks if a user exists by email.
     * @param email the user's email
     * @return true if user exists
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by email.
     * @param email the user's email
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
}

