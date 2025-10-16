package com.example.lets_play.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that extracts and validates JWT tokens from incoming
 * requests.
 *
 * <p>Looks for the {@code Authorization: Bearer <token>} header, validates
 * the token, loads user details, and populates the Spring Security
 * context. Error handling is delegated to
 * {@link com.example.lets_play.security.AuthEntryPointJwt}.</p>
 *
 * <p><strong>Security:</strong> Keep the JWT secret secure and rotate it
 * periodically. Do not log token contents.</p>
 *
 * @since 1.0
 */
public class AuthTokenFilter extends OncePerRequestFilter {
    /** Utility for JWT generation and validation. */
    @Autowired
    private JwtUtils jwtUtils;

    /** Loads user details by id for authentication population. */
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /** Logger for diagnostic messages. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthTokenFilter.class);

    /** Expected prefix for the Authorization header. */
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request,
                                    @NonNull final HttpServletResponse response,
                                    @NonNull final FilterChain filterChain)
            throws ServletException, IOException {
        try {
            final String jwt = parseJwt(request);

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                final String userId = jwtUtils.getUserIdFromJwtToken(jwt);

                final UserDetails userDetails = userDetailsService.loadUserById(userId);
                final var authorities = userDetails.getAuthorities();

                final UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (final Exception ex) {
            // Log full exception for diagnostics but avoid printing token content
            LOGGER.error("Cannot set user authentication", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(@NonNull final HttpServletRequest request) {
        final String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(BEARER_PREFIX)) {
            return headerAuth.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
