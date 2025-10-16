package com.example.lets_play.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Background service that periodically cleans up expired entries from the
 * rate limiting cache.
 *
 * <p>
 * This scheduled service invokes
 * {@link RateLimitingFilter#cleanupOldEntries} at a fixed interval to remove
 * stale request-tracking entries and prevent
 * memory growth. The schedule runs every 5 minutes by default.</p>
 *
 * @since 1.0
 */
@Service
public class RateLimitCleanupService {

    /** Rate limiting filter used to remove old entries from the in-memory
     * tracking structure. */
    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    /** Milliseconds between cleanup runs (default: 5 minutes). */
    private static final long CLEANUP_RATE_MILLIS = 5 * 60 * 1000L;

    /**
     * Run cleanup on the rate limiting filter to remove expired entries.
     *
     * <p>Scheduled to run at fixed intervals configured by
     * {@link #CLEANUP_RATE_MILLIS}.</p>
     */
    @Scheduled(fixedRate = CLEANUP_RATE_MILLIS)
    public void cleanupOldRateLimitEntries() {
        rateLimitingFilter.cleanupOldEntries();
    }
}
