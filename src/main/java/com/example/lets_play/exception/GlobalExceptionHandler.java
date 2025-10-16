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
 *
 * <p>This class centralizes mapping of exceptions to a consistent
 * {@link ErrorResponse} payload. It is safe to subclass if you need to
 * override specific handlers; when doing so prefer calling {@code super}
 * to preserve default logging and response formats.</p>
 *
 * @since 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** Logger for diagnostic information produced by this handler. */
    private static final Logger LOGGER = LoggerFactory.getLogger(
        GlobalExceptionHandler.class);

    /**
     * Handle {@link BadRequestException} and return a JSON error body.
     *
     * @param ex the caught BadRequestException
     * @param request the HTTP servlet request for the current call
     * @return ResponseEntity containing an {@link ErrorResponse} and the
     *     HTTP 400 status
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
        final BadRequestException ex,
        final HttpServletRequest request) {
        final int status = HttpStatus.BAD_REQUEST.value();
        final String error = HttpStatus.BAD_REQUEST.getReasonPhrase();
        final String path = request.getRequestURI();
        final ErrorResponse body = new ErrorResponse(
            status,
            error,
            ex.getMessage(),
            path
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle {@link ForbiddenException} and return a JSON error body with
     * HTTP 403.
     *
     * @param ex the caught ForbiddenException
     * @param request the HTTP servlet request for the current call
     * @return ResponseEntity containing an {@link ErrorResponse} and the
     *     HTTP 403 status
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
        final ForbiddenException ex,
        final HttpServletRequest request) {
        final int status = HttpStatus.FORBIDDEN.value();
        final String error = HttpStatus.FORBIDDEN.getReasonPhrase();
        final String path = request.getRequestURI();
        final ErrorResponse body = new ErrorResponse(
            status,
            error,
            ex.getMessage(),
            path
        );

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle {@link ResourceNotFoundException} and return a 404 JSON error
     * body.
     *
     * @param ex the caught ResourceNotFoundException
     * @param request the HTTP servlet request for the current call
     * @return ResponseEntity containing an {@link ErrorResponse} and the
     *     HTTP 404 status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        final ResourceNotFoundException ex,
        final HttpServletRequest request) {
        final int status = HttpStatus.NOT_FOUND.value();
        final String error = HttpStatus.NOT_FOUND.getReasonPhrase();
        final String path = request.getRequestURI();
        final ErrorResponse body = new ErrorResponse(
            status,
            error,
            ex.getMessage(),
            path
        );

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Catch-all handler to avoid leaking stack traces to clients.
     *
     * @param ex the unexpected exception
     * @param request the HTTP servlet request
     * @return ResponseEntity containing an {@link ErrorResponse} and the
     *     HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(
        final Exception ex,
        final HttpServletRequest request) {
        // Catch-all to avoid leaking 5xx errors without controlled body
    // Log the exception stack trace for diagnostics
        final String logMsg = "Unhandled exception caught by "
            + "GlobalExceptionHandler";
        LOGGER.error(logMsg, ex);

        final int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        final String error = HttpStatus.INTERNAL_SERVER_ERROR
            .getReasonPhrase();
        final String message = "An unexpected error occurred";
        final ErrorResponse body = new ErrorResponse(
            status,
            error,
            message,
            request.getRequestURI()
        );

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle validation failures for @Valid method arguments.
     *
     * <p>This method is safe to override; if you do so ensure you preserve
     * the response format (an {@link ErrorResponse}) and the HTTP status
     * semantics. Prefer calling {@code super} and augmenting the result
     * rather than replacing it entirely to maintain consistent client
     * behavior.</p>
     *
     * @param ex the validation exception
     * @param headers HTTP headers for the request
     * @param status the HTTP status code the framework computed
     * @param request the current web request
     * @return a ResponseEntity containing validation error details and the
     *     HTTP 400 status
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        @NonNull final MethodArgumentNotValidException ex,
        @NonNull final HttpHeaders headers,
        @NonNull final org.springframework.http.HttpStatusCode status,
        @NonNull final WebRequest request) {
        final var fieldErrors = ex.getBindingResult().getFieldErrors();

        final String errors = fieldErrors.stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining(", "));

        final int statusCode = org.springframework.http.HttpStatus
            .BAD_REQUEST.value();
        final String reason = org.springframework.http.HttpStatus
            .BAD_REQUEST.getReasonPhrase();
        final String path = request.getDescription(false).replace("uri=", "");
        final ErrorResponse body = new ErrorResponse(
            statusCode,
            reason,
            errors,
            path
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
