package com.example.lets_play.security;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Application-level contract for the security principal.
 *
 * <p>
 * Extends Spring Security's {@link UserDetails} and exposes
 * application-specific accessors such as {@code getId()} and
 * {@code getName()} so controllers can depend on a stable interface
 * instead of concrete implementation classes.
 * </p>
 */
public interface AppUserPrincipal extends UserDetails {
    /**
     * Unique identifier for the principal (usually the user id).
     *
     * @return the unique identifier for this principal
     */
    String getId();

    /**
     * Human-readable name for the principal (display name).
     *
     * @return the display name of this principal
     */
    String getName();
}
