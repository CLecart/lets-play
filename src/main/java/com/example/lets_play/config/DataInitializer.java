package com.example.lets_play.config;

import com.example.lets_play.model.User;
import com.example.lets_play.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Application startup initializer that seeds the database with example users.
 *
 * <p>Creates default admin and regular user accounts when the application starts,
 * if they do not already exist. Intended for development and testing only.
 * Disable or protect this component in production environments.</p>
 *
 * <p><strong>Best practices:</strong> Avoid seeding real credentials in production;
 * prefer environment-based configuration or secure onboarding flows.</p>
 *
 * @since 1.0
 */
@Component
public class DataInitializer implements CommandLineRunner {

    /** Repository for user persistence operations. */
    @Autowired
    private UserRepository userRepository;

    /** Password encoder used to hash seeded user passwords. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Seed initial user accounts when the application starts.
     *
     * @param args startup arguments (ignored)
     */
    @Override
    public void run(final String... args) throws Exception {
        // Create admin user if it doesn't exist
        if (!userRepository.existsByEmail("admin@example.com")) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("Admin user created: admin@example.com / admin123");
        }

        // Create regular user if it doesn't exist
        if (!userRepository.existsByEmail("user@example.com")) {
            User user = new User();
            user.setName("Regular User");
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole("USER");
            userRepository.save(user);
            System.out.println("Regular user created: user@example.com / user123");
        }
    }
}
