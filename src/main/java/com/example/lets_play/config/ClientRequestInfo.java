package com.example.lets_play.config;

/**
 * Benign placeholder top-level class. The real per-request tracking type is
 * kept private inside {@link RateLimitingFilter} to avoid exposing internals.
 */
public final class ClientRequestInfo {
    private ClientRequestInfo() {
        // utility class - do not instantiate
    }
}
