package com.example.lets_play.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user is authenticated but not authorized to
 * perform an action.
 *
 * <p>Mapped to HTTP 403 (Forbidden). Use this exception when the caller
 * is authenticated but lacks permission (for example, trying to modify
 * a resource owned by another user).</p>
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    /**
     * Create a new ForbiddenException with the provided message.
     *
     * @param message the error message describing the authorization failure
     */
    public ForbiddenException(final String message) {
        super(message);
    }
}
