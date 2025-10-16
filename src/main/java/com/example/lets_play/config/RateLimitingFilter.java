package com.example.lets_play.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import static com.example.lets_play.config.FilterOrder.RATE_LIMITING;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filter that enforces a simple sliding-window rate limit per client IP
 * + URI.
 */
@Component
@Order(RATE_LIMITING)
public final class RateLimitingFilter extends OncePerRequestFilter {

    /** HTTP status returned when the client exceeded the allowed rate. */
    private static final int TOO_MANY_REQUESTS_STATUS = 429;

    /**
     * JSON payload returned in the response body when rate limit is
     * exceeded.
     */
    private static final String RATE_LIMIT_JSON_PAYLOAD =
            "{\"error\":\"Rate limit exceeded\","
            + "\"message\":\"Too many requests.\"}";

    /** Number of allowed requests per minute for a single client. */
    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    /**
     * Window (in minutes) after which rate limit entries are
     * considered stale.
     */
    private static final int DEFAULT_CLEANUP_WINDOW_MINUTES = 2;

    /** Per-client (IP + URI) request counters and window start times. */
    private final Map<String, ClientRequestInfo> clientRequestCounts =
        new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final FilterChain filterChain)
            throws ServletException, IOException {

        final String clientIp = getClientIpAddress(request);
        final String clientKey = clientIp
                + ':'
                + request.getRequestURI();

        if (isRateLimited(clientKey)) {
            response.setStatus(TOO_MANY_REQUESTS_STATUS);
            response.setContentType("application/json");
            response.getWriter().write(RATE_LIMIT_JSON_PAYLOAD);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(@NonNull final String clientKey) {
        final LocalDateTime now = LocalDateTime.now();

        clientRequestCounts.compute(
                clientKey,
                (key, info) -> {
                    if (info == null) {
                        return new ClientRequestInfo(now, 1);
                    }

                    if (ChronoUnit.MINUTES.between(
                            info.getWindowStart(), now)
                            >= 1) {
                        return new ClientRequestInfo(now, 1);
                    }

                    info.incrementCount();
                    return info;
                }
        );

        final ClientRequestInfo info = clientRequestCounts.get(clientKey);
        if (info == null) {
            return false;
        }

        return info.getRequestCount() > requestsPerMinute;
    }

    private String getClientIpAddress(
            @NonNull final HttpServletRequest request) {
        final String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null
                && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        final String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null
                && !xRealIp.isBlank()) {
            return xRealIp;
        }

        return Objects.requireNonNullElse(request.getRemoteAddr(), "unknown");
    }

    /**
     * Cleanup method intended to be called periodically by a scheduled task.
     *
     * <p>Removes stale entries older than the configured cleanup window.</p>
     */
    public void cleanupOldEntries() {
        final LocalDateTime now = LocalDateTime.now();

        clientRequestCounts.entrySet()
                .removeIf(entry -> {
                    return ChronoUnit.MINUTES.between(
                            entry.getValue().getWindowStart(), now)
                            > DEFAULT_CLEANUP_WINDOW_MINUTES;
                });
    }

    private static final class ClientRequestInfo {
    /** Window start timestamp for this client's current bucket. */
    private final LocalDateTime windowStart;

    /** Number of requests observed in the current window. */
    private int requestCount;

        ClientRequestInfo(
                final LocalDateTime windowStartParam,
                final int requestCountParam) {
            this.windowStart = windowStartParam;
            this.requestCount = requestCountParam;
        }

        LocalDateTime getWindowStart() {
            return windowStart;
        }

        int getRequestCount() {
            return requestCount;
        }

        void incrementCount() {
            this.requestCount++;
        }
    }
}
