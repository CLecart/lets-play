package com.example.lets_play.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized exception handler that returns a consistent JSON error
 * schema.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle BadRequestException and return JSON error body.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(final BadRequestException ex,
                                                          final HttpServletRequest request) {
        final int status = HttpStatus.BAD_REQUEST.value();
        final String error = HttpStatus.BAD_REQUEST.getReasonPhrase();
        final ErrorResponse body = new ErrorResponse(status, error, ex.getMessage(), request.getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle ForbiddenException and return JSON error body with 403.
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(final ForbiddenException ex,
                                                         final HttpServletRequest request) {
        final int status = HttpStatus.FORBIDDEN.value();
        final String error = HttpStatus.FORBIDDEN.getReasonPhrase();
        final ErrorResponse body = new ErrorResponse(status, error, ex.getMessage(), request.getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle ResourceNotFoundException and return 404 JSON error body.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(final ResourceNotFoundException ex,
                                                        final HttpServletRequest request) {
        final int status = HttpStatus.NOT_FOUND.value();
        final String error = HttpStatus.NOT_FOUND.getReasonPhrase();
        final ErrorResponse body = new ErrorResponse(status, error, ex.getMessage(), request.getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Catch-all handler to avoid leaking stack traces to clients.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(final Exception ex,
                                                   final HttpServletRequest request) {
        // Catch-all to avoid leaking 5xx errors without controlled body
        // Log the exception stack trace for diagnostics
        LOGGER.error("Unhandled exception caught by GlobalExceptionHandler", ex);

        final int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        final String error = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
        final String message = "An unexpected error occurred";
        final ErrorResponse body = new ErrorResponse(status, error, message, request.getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull final MethodArgumentNotValidException ex,
                   @NonNull final HttpHeaders headers,
                   @NonNull final org.springframework.http.HttpStatusCode status,
                   @NonNull final WebRequest request) {
    final var fieldErrors = ex.getBindingResult().getFieldErrors();

    final String errors = fieldErrors.stream()
        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
        .collect(Collectors.joining(", "));

    final int statusCode = org.springframework.http.HttpStatus.BAD_REQUEST.value();
    final String reason = org.springframework.http.HttpStatus.BAD_REQUEST.getReasonPhrase();
    final String path = request.getDescription(false).replace("uri=", "");
    final ErrorResponse body = new ErrorResponse(statusCode, reason, errors, path);

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
