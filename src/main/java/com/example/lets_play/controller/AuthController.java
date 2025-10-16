package com.example.lets_play.controller;

import com.example.lets_play.dto.JwtResponse;
import com.example.lets_play.dto.LoginRequest;
import com.example.lets_play.dto.UserCreateRequest;
import com.example.lets_play.dto.UserResponse;
import com.example.lets_play.security.JwtUtils;
import com.example.lets_play.security.AppUserPrincipal;
import com.example.lets_play.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication and user registration.
 *
 * <p>
 * Provides endpoints for sign-in and sign-up. It issues JWT tokens and
 * integrates with Spring Security for authentication management.
 * </p>
 *
 * <p>
 * Supports CORS with a configurable maxAge and accepts requests from any
 * origin. All endpoints are under {@code /api/auth}.
 * </p>
 *
 * <p><strong>API Note:</strong> This controller is stateless and uses JWT
 * tokens for authentication.</p>
 *
 * <p><strong>Implementation Note:</strong> Requests are validated using
 * Jakarta Bean Validation annotations.</p>
 *
 * <p><strong>Security:</strong> This controller handles sensitive
 * authentication data and should be used over HTTPS in production.</p>
 *
 * @author Zone01 Developer
 * @version 1.0
 * @since 2024
 */

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    /**
     * Spring Security's authentication manager for handling authentication
     * requests.
     *
     * <p><strong>Implementation Note:</strong> Configured via
     * {@link com.example.lets_play.config.WebSecurityConfig}</p>
     */
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Service layer for user-related operations including user creation
     * and validation.
     *
     * @see com.example.lets_play.service.UserService
     */
    @Autowired
    private UserService userService;

    /**
     * Utility class for JWT token generation, validation, and parsing.
     *
     * @see com.example.lets_play.security.JwtUtils
     */
    @Autowired
    private JwtUtils jwtUtils;

    /**
    * Authenticate a user and generate a JWT token.
    *
    * <p>
    * Validates credentials and, on success, issues a JWT containing user
    * information and authorities. The token is returned with basic user
    * details so clients can store it for subsequent requests.
    * </p>
     *
    * @param loginRequest the login credentials (email + password)
    * @return a response entity with JWT token and user information on success
    * @throws BadCredentialsException when credentials are invalid
    * @throws UsernameNotFoundException when the email is not found
     * @see LoginRequest
     * @see JwtResponse
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody final LoginRequest loginRequest
    ) {
    final UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(),
            loginRequest.getPassword()
        );

    final Authentication authentication = authenticationManager.authenticate(
        authToken
    );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        final String jwt = jwtUtils.generateJwtToken(authentication);

        // Typed principal access
        final Object principal = authentication.getPrincipal();
        final String id;
        final String name;
        final String username;
        final String role;

        if (principal instanceof AppUserPrincipal appPrincipal) {
            id = appPrincipal.getId();
            name = appPrincipal.getName();
            username = appPrincipal.getUsername();
            role = resolveRoleFromPrincipal(appPrincipal);
    } else if (principal instanceof UserDetails ud) {
            id = "";
            name = "";
            username = ud.getUsername();
            role = resolveRoleFromAuthorities(ud);
        } else {
            id = "";
            name = "";
            username = "";
            role = "";
        }

        final JwtResponse resp = new JwtResponse(
                jwt,
                id,
                name,
                username,
                role
        );
        return ResponseEntity.ok(resp);
    }

    private String resolveRoleFromPrincipal(final AppUserPrincipal principal) {
        if (principal == null) {
            return "";
        }
        final var opt = principal.getAuthorities()
                .stream()
                .findFirst();

        return opt.map(a -> a.getAuthority()).orElse("");
    }

    private String resolveRoleFromAuthorities(
            final org.springframework.security.core.userdetails.UserDetails ud
    ) {
        if (ud == null) {
            return "";
        }
        final var opt = ud.getAuthorities()
                .stream()
                .findFirst();

        return opt.map(a -> a.getAuthority()).orElse("");
    }

    /**
     * Registers a new user account in the system.
     *
     * <p>
     * This endpoint creates a new user account with the provided information.
     * It validates the request data, checks for email uniqueness, encrypts the
     * password using BCrypt, and stores the user in the database with
     * appropriate default roles.
     * </p>
     *
     * @param signUpRequest the user registration data including name, email,
     *                      and password
     * @return ResponseEntity containing the created user information
     *         (without password)
     *
     * @throws com.example.lets_play.exception.BadRequestException if the email
     *         is already registered or validation fails
     * @throws jakarta.validation.ConstraintViolationException if the request
     *         data fails validation constraints
     *
     * <p><strong>API Note:</strong> Newly registered users are assigned the
     * "USER" role by default</p>
     * <p><strong>Implementation Note:</strong> Password is automatically
     * encrypted using BCrypt before storage</p>
     * <p><strong>Security:</strong> Email uniqueness is enforced at both
     * application and database levels</p>
     *
     * @see UserCreateRequest
     * @see UserResponse
    * @see UserService#createUser(UserCreateRequest)
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody final UserCreateRequest signUpRequest
    ) {
        final UserResponse user = userService.createUser(signUpRequest);
        return ResponseEntity.ok(user);
    }
}
