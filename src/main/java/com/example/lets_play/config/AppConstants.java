package com.example.lets_play.config;

/**
 * Centralized application constants to avoid magic numbers and strings.
 */
public final class AppConstants {
    private AppConstants() {
        // utility class
    }

    /** Minimum allowed length for user display name. */
    public static final int USER_NAME_MIN = 2;

    /** Maximum allowed length for user display name. */
    public static final int USER_NAME_MAX = 50;

    /** Minimum allowed length for user passwords. */
    public static final int USER_PASSWORD_MIN = 6;

    /** Maximum allowed length for email fields. */
    public static final int EMAIL_MAX = 100;

    /** Minimum allowed length for product name. */
    public static final int PRODUCT_NAME_MIN = 2;

    /** Maximum allowed length for product name. */
    public static final int PRODUCT_NAME_MAX = 100;

    /** Maximum allowed length for product description. */
    public static final int PRODUCT_DESCRIPTION_MAX = 500;
}

