package com.example.lets_play.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
/**
 * Exception thrown when a request is malformed or violates business rules.
 *
 * <p>Mapped to HTTP 400 (Bad Request). Use this exception for client-side errors
 * such as validation failures or forbidden state transitions initiated by the client.</p>
 *
 * @since 1.0
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}