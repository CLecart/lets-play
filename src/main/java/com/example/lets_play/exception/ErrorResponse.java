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

    /**
     * Default constructor for frameworks that require a no-arg
     * constructor when deserializing or constructing the object.
     */
    public ErrorResponse() { }

    /**
     * Full constructor.
     *
     * @param statusParam HTTP status code
     * @param errorParam short reason phrase
     * @param messageParam human-readable message
     * @param pathParam request path
     */
    public ErrorResponse(final int statusParam,
                         final String errorParam,
                         final String messageParam,
                         final String pathParam) {
        this.status = statusParam;
        this.error = errorParam;
        this.message = messageParam;
        this.path = pathParam;
    }

    /**
     * Returns the server timestamp when the error was created.
     *
     * @return the timestamp when this error was created
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the HTTP status code.
     *
     * @return the HTTP status code
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the HTTP status code.
     *
     * @param statusParam the HTTP status code
     */
    public void setStatus(final int statusParam) {
        this.status = statusParam;
    }

    /**
     * Returns the short HTTP reason phrase.
     *
     * @return the short HTTP reason phrase
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the short HTTP reason phrase.
     *
     * @param errorParam the short reason phrase
     */
    public void setError(final String errorParam) {
        this.error = errorParam;
    }

    /**
     * Returns the human-readable message.
     *
     * @return the human-readable message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the human-readable message.
     *
     * @param messageParam the human-readable message
     */
    public void setMessage(final String messageParam) {
        this.message = messageParam;
    }

    /**
     * Returns the request path that caused the error.
     *
     * @return the request path that caused the error
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the request path that caused the error.
     *
     * @param pathParam the request path that caused the error
     */
    public void setPath(final String pathParam) {
        this.path = pathParam;
    }
}
