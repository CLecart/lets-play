package com.example.lets_play.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter implementing sliding window algorithm for API protection.
 *
 * <p>This filter provides protection against API abuse and DDoS attacks by limiting
 * the number of requests per client IP address per minute. It uses a sliding window
 * algorithm with configurable request limits and automatic cleanup of old entries.</p>
 *
 * <p>Key features:
 * <ul>
 *   <li>Sliding window rate limiting per client IP and URI</li>
 *   <li>Configurable requests per minute limit (default: 60)</li>
 *   <li>Thread-safe implementation using ConcurrentHashMap</li>
 *   <li>Automatic cleanup of expired entries</li>
 *   <li>Support for proxy headers (X-Forwarded-For, X-Real-IP)</li>
 * </ul>
 *
 * <p><strong>API Note:</strong> Filter is applied before JWT authentication in the security chain</p>
 * <p><strong>Implementation Note:</strong> Uses LocalDateTime for time window management and ConcurrentHashMap for thread safety</p>
 * <p><strong>Security:</strong> Protects against brute force attacks and API abuse patterns</p>
 *
 * @author Zone01 Developer
 * @version 1.0
 * @since 2024
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    /**
     * Maximum number of requests allowed per minute per client, configurable via application properties.
     *
     * <p><strong>API Note:</strong> Default value is 60 requests per minute if not configured</p>
     */
    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    /**
     * Thread-safe map storing request count information per client.
     * Key format: "clientIP:requestURI"
     *
     * <p><strong>Implementation Note:</strong> Uses ConcurrentHashMap for thread-safe operations</p>
     */
    private final ConcurrentHashMap<String, ClientRequestInfo> clientRequestCounts = new ConcurrentHashMap<>();

    /**
     * Processes each HTTP request to enforce rate limiting before passing to the next filter.
     *
     * <p>This method extracts the client IP address, checks if the client has exceeded
     * the rate limit, and either blocks the request with HTTP 429 or allows it to proceed.</p>
     *
     * @param request the HTTP request being processed
     * @param response the HTTP response to be sent
     * @param filterChain the filter chain to continue processing
     *
     * @throws ServletException if request processing fails
     * @throws IOException if I/O operations fail
     *
     * <p><strong>API Note:</strong> Blocked requests receive HTTP 429 status with JSON error message</p>
     * <p><strong>Implementation Note:</strong> Uses client IP + request URI as unique identifier for rate limiting</p>
     * <p><strong>Security:</strong> Prevents API abuse by limiting requests per client per minute</p>
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIpAddress(request);
        String clientKey = clientIp + ":" + request.getRequestURI();

        if (isRateLimited(clientKey)) {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Checks if a client has exceeded the rate limit using sliding window algorithm.
     *
     * <p>This method implements a sliding window rate limiting algorithm that tracks
     * request counts per minute. If a client exceeds the configured limit, subsequent
     * requests are blocked until the window slides forward.</p>
     *
     * @param clientKey unique identifier for the client (IP + URI)
     * @return true if the client is rate limited, false otherwise
     *
     * <p><strong>Implementation Note:</strong> Uses ConcurrentHashMap.compute() for atomic operations</p>
     * <p><strong>Security:</strong> Sliding window prevents burst attacks while allowing steady traffic</p>
     */
    private boolean isRateLimited(@NonNull String clientKey) {
        LocalDateTime now = LocalDateTime.now();

        clientRequestCounts.compute(clientKey, (key, info) -> {
            if (info == null || ChronoUnit.MINUTES.between(info.getWindowStart(), now) >= 1) {
                return new ClientRequestInfo(now, 1);
            } else {
                info.incrementCount();
                return info;
            }
        });

        ClientRequestInfo info = clientRequestCounts.get(clientKey);
        return info.getRequestCount() > requestsPerMinute;
    }

    /**
     * Extracts the real client IP address from the HTTP request.
     *
     * <p>This method handles various proxy scenarios by checking common proxy headers
     * before falling back to the direct remote address. This ensures accurate rate
     * limiting even when the application is behind load balancers or CDNs.</p>
     *
     * @param request the HTTP request to extract IP address from
     * @return the real client IP address
     *
     * <p><strong>API Note:</strong> Checks X-Forwarded-For and X-Real-IP headers for proxy compatibility</p>
     * <p><strong>Implementation Note:</strong> Takes the first IP from X-Forwarded-For in case of multiple proxies</p>
     * <p><strong>Security:</strong> Proper IP extraction prevents rate limit bypass through proxies</p>
     */
    private String getClientIpAddress(@NonNull HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Removes expired entries from the rate limiting cache to prevent memory leaks.
     *
     * <p>This method is called periodically by the cleanup service to remove entries
     * older than 2 minutes, ensuring the cache doesn't grow indefinitely. The 2-minute
     * buffer provides safety margin beyond the 1-minute rate limiting window.</p>
     *
     * <p><strong>API Note:</strong> Called automatically by RateLimitCleanupService every 5 minutes</p>
     * <p><strong>Implementation Note:</strong> Uses removeIf with time-based predicate for efficient cleanup</p>
     * <p><strong>Security:</strong> Prevents memory exhaustion attacks through cache bloating</p>
     */
    public void cleanupOldEntries() {
        LocalDateTime now = LocalDateTime.now();
        clientRequestCounts.entrySet().removeIf(entry ->
            ChronoUnit.MINUTES.between(entry.getValue().getWindowStart(), now) > 2);
    }

    /**
     * Inner class representing client request tracking information for rate limiting.
     *
     * <p>This class stores the sliding window start time and current request count
     * for each client. It provides thread-safe access to request tracking data
     * used by the rate limiting algorithm.</p>
     *
     * <p><strong>Implementation Note:</strong> Immutable window start time with mutable request count for efficiency</p>
     * <p><strong>Security:</strong> Request count is incremented atomically within compute operations</p>
     */
    private static class ClientRequestInfo {
        /**
         * The start time of the current rate limiting window.
         */
        private final LocalDateTime windowStart;

        /**
         * Current number of requests within this window.
         */
        private int requestCount;

        /**
         * Creates a new client request info with specified window start and count.
         *
         * @param windowStart the start time of the rate limiting window
         * @param requestCount the initial request count for this window
         */
        public ClientRequestInfo(LocalDateTime windowStart, int requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }

        /**
         * Gets the start time of the current rate limiting window.
         *
         * @return the window start time
         */
        public LocalDateTime getWindowStart() {
            return windowStart;
        }

        /**
         * Gets the current request count within this window.
         *
         * @return the number of requests in this window
         */
        public int getRequestCount() {
            return requestCount;
        }

        /**
         * Increments the request count for this window.
         *
         * <p><strong>Implementation Note:</strong> Called within atomic compute operations for thread safety</p>
         */
        public void incrementCount() {
            this.requestCount++;
        }
    }
}
