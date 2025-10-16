package com.example.lets_play;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application bootstrap for the Lets Play service.
 *
 * <p>This class is the primary entry point for the Spring Boot application.
 * It enables scheduling support for background tasks and starts the embedded
 * server when executed. Keep this class minimal — do not add business logic
 * here.</p>
 *
 * <p>Usage:
 * <pre>
 * java -jar lets-play.jar
 * </pre>
 * </p>
 *
 * <p><strong>Best practices:</strong>
 * <ul>
 *   <li>Keep this class free of heavy initialization logic. Use
 *   ApplicationRunner or CommandLineRunner for startup tasks.</li>
 *   <li>Enable features (like scheduling) via annotations only as needed.</li>
 * </ul>
 * </p>
 *
 * @since 1.0
 */
@SpringBootApplication
@EnableScheduling
public final class LetsPlayApplication {

    /**
     * Private constructor to prevent instantiation of this utility bootstrap class.
     */
    private LetsPlayApplication() {
        // intentionally empty
    }

    /**
     * Application main entry point.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(final String[] args) {
        SpringApplication.run(LetsPlayApplication.class, args);
    }

}
