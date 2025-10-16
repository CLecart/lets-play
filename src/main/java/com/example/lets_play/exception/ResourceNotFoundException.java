package com.example.lets_play.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource cannot be found.
 *
 * <p>Mapped to HTTP 404 (Not Found). Use this exception when an entity
 * lookup fails and the server needs to indicate to the client that the
 * resource does not exist.</p>
 *
 * @since 1.0
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Create a new exception with the given message.
     *
     * @param message human-readable error message
     */
    public ResourceNotFoundException(final String message) {
        super(message);
    }

    /**
     * Create an exception describing a missing entity by identifying the
     * resource, field and value that were used for the lookup.
     *
     * @param resourceName the type or name of the missing resource
     * @param fieldName name of the field used in the lookup
     * @param fieldValue value of the lookup field
     */
    public ResourceNotFoundException(final String resourceName,
        final String fieldName, final Object fieldValue) {
        super(String.format(
            "%s not found with %s : '%s'",
            resourceName,
            fieldName,
            fieldValue
        ));
    }
}
