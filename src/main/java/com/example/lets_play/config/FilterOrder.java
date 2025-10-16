package com.example.lets_play.config;

/**
 * Holds common filter order constants to avoid magic numbers in annotations.
 */
public final class FilterOrder {
    /** Order used for the rate limiting filter (lower means earlier invocation). */
    public static final int RATE_LIMITING = 100;

    private FilterOrder() {
        // utility class
    }
}
