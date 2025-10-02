package com.example.lets_play.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    private final ConcurrentHashMap<String, ClientRequestInfo> clientRequestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
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

    private boolean isRateLimited(String clientKey) {
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

    private String getClientIpAddress(HttpServletRequest request) {
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

    // Nettoyer les anciennes entrées périodiquement
    public void cleanupOldEntries() {
        LocalDateTime now = LocalDateTime.now();
        clientRequestCounts.entrySet().removeIf(entry -> 
            ChronoUnit.MINUTES.between(entry.getValue().getWindowStart(), now) > 2);
    }

    private static class ClientRequestInfo {
        private final LocalDateTime windowStart;
        private int requestCount;

        public ClientRequestInfo(LocalDateTime windowStart, int requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }

        public LocalDateTime getWindowStart() {
            return windowStart;
        }

        public int getRequestCount() {
            return requestCount;
        }

        public void incrementCount() {
            this.requestCount++;
        }
    }
}