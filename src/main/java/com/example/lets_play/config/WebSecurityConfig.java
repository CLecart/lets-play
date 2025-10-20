package com.example.lets_play.config;

import com.example.lets_play.security.AuthEntryPointJwt;
import com.example.lets_play.security.AuthTokenFilter;
import com.example.lets_play.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security configuration for JWT authentication and
 * authorization.
 *
 * <p>Provides a stateless security setup for the REST API. This
 * includes JWT processing, CORS configuration and rate limiting.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    prePostEnabled = true
)
public class WebSecurityConfig {

    /**
     * Custom user details service for loading user-specific data during
     * authentication.
     *
     * @see com.example.lets_play.security.UserDetailsServiceImpl
     */
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Accessor for the injected user details service. Provided for Checkstyle
     * visibility rules and potential external usage in tests.
     *
     * @return the configured UserDetailsServiceImpl instance
     */
    public UserDetailsServiceImpl getUserDetailsService() {
        return userDetailsService;
    }

    /**
     * Custom authentication entry point for handling unauthorized access
     * attempts.
     *
     * @see com.example.lets_play.security.AuthEntryPointJwt
     */
    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    /**
     * Rate limiting filter for protecting against API abuse and DDoS attacks.
     *
     * @see com.example.lets_play.config.RateLimitingFilter
     */
    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    /**
     * Flag-controlled filter that enforces HTTPS requests when enabled.
     *
     * <p>Configured as a bean elsewhere and injected here so the filter
     * may be placed early in the security filter chain.</p>
     */
    @Autowired
    private HttpsEnforcerFilter httpsEnforcerFilter;

    /** Comma-separated allowed CORS origins from application properties. */
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /** Feature flag that enforces HTTPS when true. Default false. */
    @Value("${app.security.force-https:false}")
    private boolean forceHttps;

    /**
     * Creates the JWT authentication filter bean.
     *
     * <p>Extracts and validates tokens from the Authorization header and
     * populates the SecurityContext on success.</p>
     *
     * @see com.example.lets_play.security.AuthTokenFilter
     * @return a configured {@link AuthTokenFilter} instance
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }



    /**
     * Exposes AuthenticationManager used for programmatic authentication.
     *
    * @param authConfig the authentication configuration used to obtain the
    *                   manager
     * @return the configured {@link AuthenticationManager}
     * @throws Exception when the AuthenticationManager cannot be created
     * @see com.example.lets_play.controller.AuthController#authenticateUser
     */
    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Returns a BCrypt PasswordEncoder (default strength).
     *
     * @return a {@link PasswordEncoder} that uses BCrypt hashing
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) settings.
     *
     * <p>Supports origin patterns, common methods and credentials.</p>
     *
     * @return a {@link CorsConfigurationSource} with the configured CORS
     *         rules
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow a special value '*' or 'all' to permit any origin. Otherwise
        // split and trim the comma-separated list from properties.
        final var trimmed = allowedOrigins == null ? "" : allowedOrigins.trim();
        final List<String> originPatterns;
        if ("*".equals(trimmed) || "all".equalsIgnoreCase(trimmed)) {
            originPatterns = List.of("*");
        } else {
            originPatterns = Arrays.stream(trimmed.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        }
        configuration.setAllowedOriginPatterns(originPatterns);

        final var methods = Arrays.asList(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS"
        );
        configuration.setAllowedMethods(methods);

        final var headers = Arrays.asList("*");
        configuration.setAllowedHeaders(headers);
        configuration.setAllowCredentials(true);

        final UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configure the main SecurityFilterChain for HTTP requests.
     *
     * <p>Sets up CORS, disables CSRF, selects stateless sessions and
     * applies authorization rules and filters.</p>
     *
     * @param http the HttpSecurity configuration
     * @return configured SecurityFilterChain
     * @throws Exception on configuration error
     */
    @Bean
    public SecurityFilterChain filterChain(
        final HttpSecurity http)
        throws Exception {
        // Configure CORS
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // Disable CSRF for stateless JWT APIs
        http.csrf(csrf -> csrf.disable());

    // Custom authentication entry point for unauthorized access
    http.exceptionHandling(
        exception -> exception.authenticationEntryPoint(unauthorizedHandler)
    );

    // Stateless session management
    http.sessionManagement(
        session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    );

        // Authorization rules
        http.authorizeHttpRequests(authz -> {
            authz.requestMatchers("/api/auth/**").permitAll();
            authz.requestMatchers("/api/products/**").permitAll();
            authz.requestMatchers("/error").permitAll();
            authz.anyRequest().authenticated();
        });

    // Add HTTPS enforcer early in the chain when enabled.
    http.addFilterBefore(
        httpsEnforcerFilter,
        UsernamePasswordAuthenticationFilter.class
    );

    // JWT authentication filter should run before the default
    // UsernamePasswordAuthenticationFilter.
    http.addFilterBefore(
        authenticationJwtTokenFilter(),
        UsernamePasswordAuthenticationFilter.class
    );

    http.addFilterBefore(
        rateLimitingFilter,
        AuthTokenFilter.class
    );

        // Note: HTTPS enforcement behavior is controlled by the
        // HttpsEnforcerFilter implementation and the feature flag.

        return http.build();
    }
}
