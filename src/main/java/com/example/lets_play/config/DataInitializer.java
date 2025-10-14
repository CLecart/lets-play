package com.example.lets_play.config;

import com.example.lets_play.model.User;
import com.example.lets_play.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
/**
 * Application startup initializer that seeds the database with example users.
 *
 * <p>This component creates default administrative and regular user accounts when the
 * application starts if they do not already exist. It is intended for development
 * and testing environments and should be disabled or protected in production.</p>
 *
 * <strong>Best practices:</strong> Avoid seeding real credentials in production and
 * instead use environment-based configuration or secure onboarding flows.
 *
 * @since 1.0
 */
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
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