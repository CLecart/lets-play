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

/**
 * Optional filter that enforces HTTPS by redirecting HTTP requests to the
 * equivalent HTTPS URL. Honors X-Forwarded-Proto for proxied setups.
 *
 * This filter is feature-flagged via {@code app.security.force-https} so
 * the default developer experience (false) is unchanged.
 */
@Component
public final class HttpsEnforcerFilter extends OncePerRequestFilter {

    /** When true, redirect HTTP requests to the equivalent HTTPS URL. */
    @Value("${app.security.force-https:false}")
    private boolean forceHttps;

    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request,
                                    @NonNull final HttpServletResponse response,
                                    @NonNull final FilterChain filterChain)
            throws ServletException, IOException {

        if (forceHttps && !isSecure(request)) {
            final String host = request.getHeader("Host");
            final String requestURI = request.getRequestURI();
            final String query = request.getQueryString();

        final String hostOrServer = (host == null)
            ? request.getServerName()
            : host;

        final String redirectTo = new StringBuilder()
            .append("https://")
            .append(hostOrServer)
            .append(requestURI)
            .append(query == null ? "" : "?" + query)
            .toString();

            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", redirectTo);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSecure(final HttpServletRequest request) {
        if (request.isSecure()) {
            return true;
        }

        final String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedProto == null) {
            return false;
        }

        return forwardedProto.equalsIgnoreCase("https");
    }
}
