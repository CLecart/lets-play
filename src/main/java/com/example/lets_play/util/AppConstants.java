package com.example.lets_play.util;

/**
 * Application-wide constants used in annotations and configuration where a
 * compile-time constant is required (for example, annotation attributes).
 */
public final class AppConstants {

    private AppConstants() {
        // utility class; prevent instantiation
    }

    /** CORS preflight max-age (seconds). */
    public static final int CORS_MAX_AGE_SECONDS = 3600;
}
