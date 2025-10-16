package com.example.lets_play.repository;

import com.example.lets_play.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    /**
     * Finds a user by their email address.
     *
     * @param email the user's email address
    * @return an Optional containing the User if found, otherwise an empty
    *         Optional
     * @since 1.0
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email exists.
     *
     * @param email the email address to check
     * @return true if a user with the email exists, false otherwise
     * @since 1.0
     */
    boolean existsByEmail(String email);
}
