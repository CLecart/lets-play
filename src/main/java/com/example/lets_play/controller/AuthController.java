package com.example.lets_play.controller;

import com.example.lets_play.dto.JwtResponse;
import com.example.lets_play.dto.LoginRequest;
import com.example.lets_play.dto.UserCreateRequest;
import com.example.lets_play.dto.UserResponse;
import com.example.lets_play.security.JwtUtils;
import com.example.lets_play.security.UserPrincipal;
import com.example.lets_play.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling authentication and user registration operations.
 * 
 * <p>This controller provides endpoints for user authentication (sign-in) and user registration (sign-up).
 * It handles JWT token generation and validation, and integrates with Spring Security for authentication
 * management.</p>
 * 
 * <p>The controller supports CORS requests with a maximum age of 3600 seconds and accepts requests from
 * any origin. All endpoints are mapped under the {@code /api/auth} path.</p>
 * 
 * @apiNote This controller is stateless and uses JWT tokens for authentication.
 * @implNote All requests are validated using Jakarta Bean Validation annotations.
 * @security This controller handles sensitive authentication data and should be used over HTTPS in production.
 * 
 * @author Zone01 Developer
 * @version 1.0
 * @since 2024
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Spring Security's authentication manager for handling authentication requests.
     * 
     * @implNote Configured via {@link com.example.lets_play.config.WebSecurityConfig}
     */
    @Autowired
    AuthenticationManager authenticationManager;

    /**
     * Service layer for user-related operations including user creation and validation.
     * 
     * @see com.example.lets_play.service.UserService
     */
    @Autowired
    UserService userService;

    /**
     * Utility class for JWT token generation, validation, and parsing.
     * 
     * @see com.example.lets_play.security.JwtUtils
     */
    @Autowired
    JwtUtils jwtUtils;

    /**
     * Authenticates a user and generates a JWT token for subsequent requests.
     * 
     * <p>This endpoint validates user credentials against the database and, upon successful
     * authentication, generates a JWT token containing user information and authorities.
     * The token is returned along with user details for client-side storage and usage.</p>
     * 
     * @param loginRequest the login credentials containing email and password
     * @return ResponseEntity containing JWT token and user information on success
     * 
     * @throws org.springframework.security.authentication.BadCredentialsException
     *         if the provided credentials are invalid
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException
     *         if the user email is not found in the database
     * 
     * @apiNote The generated JWT token should be included in the Authorization header
     *          for subsequent API calls as "Bearer {token}"
     * @implNote Uses Spring Security's AuthenticationManager for credential validation
     * @security Password is validated against BCrypt-encoded hash stored in database
     * 
     * @see LoginRequest
     * @see JwtResponse
     * @see UserPrincipal
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getName(),
                userDetails.getUsername(),
                userDetails.getAuthorities().iterator().next().getAuthority()));
    }

    /**
     * Registers a new user account in the system.
     * 
     * <p>This endpoint creates a new user account with the provided information.
     * It validates the request data, checks for email uniqueness, encrypts the password
     * using BCrypt, and stores the user in the database with appropriate default roles.</p>
     * 
     * @param signUpRequest the user registration data including name, email, and password
     * @return ResponseEntity containing the created user information (without password)
     * 
     * @throws com.example.lets_play.exception.BadRequestException
     *         if the email is already registered or validation fails
     * @throws jakarta.validation.ConstraintViolationException
     *         if the request data fails validation constraints
     * 
     * @apiNote Newly registered users are assigned the "USER" role by default
     * @implNote Password is automatically encrypted using BCrypt before storage
     * @security Email uniqueness is enforced at both application and database levels
     * 
     * @see UserCreateRequest
     * @see UserResponse
     * @see com.example.lets_play.service.UserService#createUser(UserCreateRequest)
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserCreateRequest signUpRequest) {
        UserResponse user = userService.createUser(signUpRequest);
        return ResponseEntity.ok(user);
    }
}