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
 * equivalent HTTPS URL. Honours X-Forwarded-Proto for proxied setups.
 *
 * This filter is feature-flagged via `app.security.force-https` so the
 * default developer experience (false) is unchanged.
 */
@Component
/**
 * Final filter that optionally enforces HTTPS for incoming requests. The
 * filter will redirect to an equivalent https:// URL when enabled.
 */
public final class HttpsEnforcerFilter extends OncePerRequestFilter {

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

        final String hostOrServer = (host == null) ? request.getServerName() : host;
        final String redirectTo = "https://" + hostOrServer
            + requestURI
            + (query == null ? "" : "?" + query);

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
        return forwardedProto != null && forwardedProto.equalsIgnoreCase("https");
    }
}
