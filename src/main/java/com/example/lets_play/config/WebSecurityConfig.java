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

/**
 * Spring Security configuration for JWT authentication and authorization.
 *
 * <p>Sets up a stateless security framework for REST APIs. The
 * configuration includes JWT authentication, CORS, and a rate limiting
 * filter. It also enables method-level security for fine-grained access
 * control.</p>
 *
 * <p>Main features include:</p>
 * <ul>
 *   <li>JWT-based stateless authentication with custom filters</li>
 *   <li>BCrypt password encoding</li>
 *   <li>Configurable CORS</li>
 *   <li>Rate limiting for abuse protection</li>
 * </ul>
 *
 * <p>CSRF is disabled because JWT-based stateless APIs do not require it.</p>
 *
 * @author Zone01 Developer
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    /**
     * Custom user details service for loading user-specific data during authentication.
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
     * Custom authentication entry point for handling unauthorized access attempts.
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

    @Autowired
    private HttpsEnforcerFilter httpsEnforcerFilter;

    /**
     * Comma-separated list of allowed CORS origins loaded from application properties.
     *
    * <p><strong>API Note:</strong> Supports wildcard patterns for flexible
    * origin matching.</p>
     */
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /** Feature flag to require HTTPS across the application when true. Default false. */
    @Value("${app.security.force-https:false}")
    private boolean forceHttps;

    /**
     * Creates and configures the JWT authentication filter bean.
     *
    * <p>This filter intercepts HTTP requests to extract and validate JWT
    * tokens from the Authorization header. Valid tokens result in setting
    * the security context with the authenticated user's details.</p>
     *
     * @return AuthTokenFilter configured JWT authentication filter
     *
    * <p><strong>API Note:</strong> This filter is added before
    * UsernamePasswordAuthenticationFilter in the chain</p>
     * @see com.example.lets_play.security.AuthTokenFilter
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }



    /**
     * Exposes the authentication manager bean for manual authentication operations.
     *
    * <p>This manager is used primarily in the authentication controller for
    * processing login requests and generating JWT tokens upon successful
    * authentication.</p>
     *
     * @param authConfig the authentication configuration provided by Spring Security
     * @return AuthenticationManager the configured authentication manager
     *
     * @throws Exception if authentication manager configuration fails
     *
    * @see com.example.lets_play.controller.AuthController#authenticateUser
     */
    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Creates the password encoder bean for secure password hashing.
     *
    * <p>Uses BCrypt algorithm with default strength (10 rounds) for password
    * hashing. BCrypt is a slow, adaptive hashing function designed to remain
    * secure against rainbow table and brute-force attacks as computing power
    * increases.</p>
     *
     * @return PasswordEncoder BCrypt password encoder with default strength
     *
    * <p><strong>API Note:</strong> Default BCrypt strength of 10 provides good
    * security/performance balance</p>
    * <p><strong>Security:</strong> BCrypt includes salt generation and is
    * resistant to rainbow table attacks</p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) for the application.
     *
    * <p>This configuration enables web apps on different domains to access the
    * API. It supports configurable origins, common HTTP methods, all headers,
    * and includes credentials for authenticated requests.</p>
     *
     * @return CorsConfigurationSource configured CORS settings for the application
     *
    * <p><strong>API Note:</strong> Allows credentials for JWT-based
    * authentication.</p>
    * <p><strong>Implementation Note:</strong> Uses origin patterns for flexible
    * matching.</p>
    * <p><strong>Security:</strong> Credential support enables secure
    * cross-origin authenticated requests.</p>
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Break long expressions into local variables to satisfy LineLength checks
    final var originList = allowedOrigins.split(",");
    final var originPatterns = Arrays.asList(originList);
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

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configures the main security filter chain for HTTP requests.
     *
     * <p>This method sets up the complete security configuration including CORS, CSRF protection,
     * exception handling, session management, and request authorization rules. It also configures
     * the filter chain order for proper request processing.</p>
     *
     * <p>Security configuration:
     * <ul>
     *   <li>CORS: Enabled with custom configuration source</li>
     *   <li>CSRF: Disabled (not needed for stateless JWT authentication)</li>
     *   <li>Sessions: Stateless (no server-side session storage)</li>
     *   <li>Authentication: JWT-based with custom entry point for unauthorized access</li>
     * </ul>
     *
     * <p>Access rules:
     * <ul>
     *   <li>/api/auth/** - Public access (login/register)</li>
     *   <li>/api/products/** - Public access (product browsing)</li>
     *   <li>/error - Public access (error handling)</li>
     *   <li>All other requests - Authentication required</li>
     * </ul>
     *
     * <p>Filter chain order:
     * <ol>
     *   <li>Rate limiting filter (API abuse protection)</li>
     *   <li>JWT authentication filter (token processing)</li>
     *   <li>Username/password authentication filter (Spring Security default)</li>
     * </ol>
     *
     * @param http the HttpSecurity configuration object
     * @return SecurityFilterChain the configured security filter chain
     *
     * @throws Exception if security configuration fails
     *
     * <p><strong>API Note:</strong> CSRF is disabled as JWT tokens provide CSRF protection for stateless APIs</p>
     * <p><strong>Implementation Note:</strong> Rate limiting filter is positioned before JWT filter for early request filtering</p>
     * <p><strong>Security:</strong> Stateless configuration prevents session fixation and reduces server memory usage</p>
     */
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        // Configure CORS
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // Disable CSRF for stateless JWT APIs
        http.csrf(csrf -> csrf.disable());

        // Custom authentication entry point for unauthorized access
        http.exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler));

        // Stateless session management
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Authorization rules
        http.authorizeHttpRequests(authz -> {
            authz.requestMatchers("/api/auth/**").permitAll();
            authz.requestMatchers("/api/products/**").permitAll();
            authz.requestMatchers("/error").permitAll();
            authz.anyRequest().authenticated();
        });

    // Add HTTPS enforcer early in the chain (feature-flagged). Use a
    // built-in anchor filter class to avoid Spring Security's "no
    // registered order" error when referencing custom filter classes.
    http.addFilterBefore(httpsEnforcerFilter, UsernamePasswordAuthenticationFilter.class);

    // Authentication is handled by JWT filter - no need for DaoAuthenticationProvider
    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(rateLimitingFilter, AuthTokenFilter.class);

    // HTTPS enforcement is handled by a dedicated filter (HttpsEnforcerFilter)
    // when `app.security.force-https` is enabled. We avoid calling
    // deprecated HttpSecurity.requiresChannel()/requiresSecure() APIs here.

        return http.build();
    }
}
