package com.example.lets_play.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Background service that periodically cleans up expired entries from the rate limiting cache.
 *
 * <p>This scheduled service invokes {@link com.example.lets_play.config.RateLimitingFilter#cleanupOldEntries}
 * at a fixed interval to remove stale request tracking entries and prevent memory growth.
 * The schedule is configured to run every 5 minutes by default.</p>
 *
 * @since 1.0
 */
@Service
public class RateLimitCleanupService {

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    private static final long CLEANUP_RATE_MILLIS = 5 * 60 * 1000L;

    // Nettoie les anciennes entrées toutes les 5 minutes
    @Scheduled(fixedRate = CLEANUP_RATE_MILLIS) // 5 minutes en millisecondes
    public void cleanupOldRateLimitEntries() {
        rateLimitingFilter.cleanupOldEntries();
    }
}
