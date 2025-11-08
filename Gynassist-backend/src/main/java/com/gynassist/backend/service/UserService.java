package com.gynassist.backend.service;

import com.gynassist.backend.entity.User;
import com.gynassist.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Checks if a user exists by email.
     * @param email the user's email
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Saves a new user to the database.
     * @param user the user entity
     * @return the saved user
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Finds a user by email.
     * @param email the user's email
     * @return Optional containing the user if found
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Retrieves a user by ID.
     * @param id the user's ID
     * @return Optional containing the user if found
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Add more user-related business logic here as needed
}
