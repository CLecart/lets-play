package com.example.lets_play.config;

import com.example.lets_play.security.AuthEntryPointJwt;
import com.example.lets_play.security.AuthTokenFilter;
import com.example.lets_play.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
 * Comprehensive Spring Security configuration for JWT-based authentication and authorization.
 * 
 * <p>This configuration class sets up a complete security framework including JWT authentication,
 * CORS configuration, rate limiting, and method-level security. It implements a stateless
 * authentication system suitable for REST APIs and modern web applications.</p>
 * 
 * <p>Key security features:
 * <ul>
 *   <li>JWT-based stateless authentication with custom filters</li>
 *   <li>BCrypt password encoding with default strength (10 rounds)</li>
 *   <li>Configurable CORS support for cross-origin requests</li>
 *   <li>Rate limiting filter for API abuse protection</li>
 *   <li>Method-level security with @PreAuthorize annotations</li>
 *   <li>Custom authentication entry point for unauthorized access</li>
 * </ul></p>
 * 
 * @apiNote This configuration disables CSRF as it's not needed for stateless JWT authentication
 * @implNote Uses filter chain approach with Spring Security 6.x configuration style
 * @security All passwords are encrypted with BCrypt, JWT tokens provide stateless authentication
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
    UserDetailsServiceImpl userDetailsService;

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

    /**
     * Comma-separated list of allowed CORS origins loaded from application properties.
     * 
     * @apiNote Supports wildcard patterns for flexible origin matching
     */
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Creates and configures the JWT authentication filter bean.
     * 
     * <p>This filter intercepts HTTP requests to extract and validate JWT tokens
     * from the Authorization header. Valid tokens result in setting the security
     * context with the authenticated user's details.</p>
     * 
     * @return AuthTokenFilter configured JWT authentication filter
     * 
     * @apiNote This filter is added before UsernamePasswordAuthenticationFilter in the chain
     * @see com.example.lets_play.security.AuthTokenFilter
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * Creates and configures the DAO authentication provider for database-based authentication.
     * 
     * <p>This provider handles username/password authentication by loading user details
     * from the database and validating credentials against stored BCrypt hashes.</p>
     * 
     * @return DaoAuthenticationProvider configured for database authentication
     * 
     * @implNote Uses custom UserDetailsService and BCrypt password encoder
     * @security Password validation performed against BCrypt hashes in database
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the authentication manager bean for manual authentication operations.
     * 
     * <p>This manager is used primarily in the authentication controller for
     * processing login requests and generating JWT tokens upon successful authentication.</p>
     * 
     * @param authConfig the authentication configuration provided by Spring Security
     * @return AuthenticationManager the configured authentication manager
     * 
     * @throws Exception if authentication manager configuration fails
     * 
     * @see com.example.lets_play.controller.AuthController#authenticateUser
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Creates the password encoder bean for secure password hashing.
     * 
     * <p>Uses BCrypt algorithm with default strength (10 rounds) for password hashing.
     * BCrypt is a slow, adaptive hashing function designed to remain secure against
     * rainbow table and brute-force attacks as computing power increases.</p>
     * 
     * @return PasswordEncoder BCrypt password encoder with default strength
     * 
     * @apiNote Default BCrypt strength of 10 provides good security/performance balance
     * @security BCrypt includes salt generation and is resistant to rainbow table attacks
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) for the application.
     * 
     * <p>This configuration allows web applications running on different domains to
     * access the API. It supports configurable allowed origins, all standard HTTP methods,
     * all headers, and credential inclusion for authenticated requests.</p>
     * 
     * @return CorsConfigurationSource configured CORS settings for the application
     * 
     * @apiNote Allows credentials for JWT token-based authentication
     * @implNote Uses origin patterns for flexible domain matching
     * @security Credential support enables secure cross-origin authenticated requests
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
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
     * </ul></p>
     * 
     * <p>Access rules:
     * <ul>
     *   <li>/api/auth/** - Public access (login/register)</li>
     *   <li>/api/products/** - Public access (product browsing)</li>
     *   <li>/error - Public access (error handling)</li>
     *   <li>All other requests - Authentication required</li>
     * </ul></p>
     * 
     * <p>Filter chain order:
     * <ol>
     *   <li>Rate limiting filter (API abuse protection)</li>
     *   <li>JWT authentication filter (token processing)</li>
     *   <li>Username/password authentication filter (Spring Security default)</li>
     * </ol></p>
     * 
     * @param http the HttpSecurity configuration object
     * @return SecurityFilterChain the configured security filter chain
     * 
     * @throws Exception if security configuration fails
     * 
     * @apiNote CSRF is disabled as JWT tokens provide CSRF protection for stateless APIs
     * @implNote Rate limiting filter is positioned before JWT filter for early request filtering
     * @security Stateless configuration prevents session fixation and reduces server memory usage
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/products/**").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(rateLimitingFilter, AuthTokenFilter.class);

        return http.build();
    }
}