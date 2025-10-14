package com.example.lets_play;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
/**
 * Main application bootstrap for the Lets Play service.
 *
 * <p>This class is the primary entry point for the Spring Boot application. It
 * enables scheduling support for background tasks and starts the embedded server
 * when executed. Keep this class minimal — do not add business logic here.</p>
 *
 * <p>Usage:
 * <pre>
 * java -jar lets-play.jar
 * </pre>
 * </p>
 *
 * <p><strong>Best practices:</strong>
 * <ul>
 *   <li>Keep this class free of heavy initialization logic; use {@link org.springframework.context.ApplicationRunner}
 *       or {@link org.springframework.boot.CommandLineRunner} for startup tasks.</li>
 *   <li>Enable features (like scheduling) via annotations only as needed.</li>
 * </ul>
 * </p>
 *
 * @since 1.0
 */
public class LetsPlayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LetsPlayApplication.class, args);
    }

}
