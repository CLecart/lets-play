package com.example.lets_play.exception;

import java.time.Instant;

/**
 * Standard error response returned by the API.
 */
public class ErrorResponse {
    /**
     * Time when the error occurred (server-side instant).
     */
    private Instant timestamp = Instant.now();

    /** HTTP status code for the error. */
    private int status;

    /** Short HTTP reason phrase (e.g. "Bad Request"). */
    private String error;

    /** Human-readable error message intended for clients. */
    private String message;

    /** Request path that caused the error. */
    private String path;

    public ErrorResponse() {}

    /**
     * Full constructor.
     *
     * @param status HTTP status code
     * @param error short reason phrase
     * @param message human-readable message
     * @param path request path
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    /** Returns the server timestamp when the error was created. */
    public Instant getTimestamp() {
        return timestamp;
    }

    /** Returns the HTTP status code. */
    public int getStatus() {
        return status;
    }

    /** Sets the HTTP status code. */
    public void setStatus(int status) {
        this.status = status;
    }

    /** Returns the short HTTP reason phrase. */
    public String getError() {
        return error;
    }

    /** Sets the short HTTP reason phrase. */
    public void setError(String error) {
        this.error = error;
    }

    /** Returns the human-readable message. */
    public String getMessage() {
        return message;
    }

    /** Sets the human-readable message. */
    public void setMessage(String message) {
        this.message = message;
    }

    /** Returns the request path that caused the error. */
    public String getPath() {
        return path;
    }

    /** Sets the request path that caused the error. */
    public void setPath(String path) {
        this.path = path;
    }
}
