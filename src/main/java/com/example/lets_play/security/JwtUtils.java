package com.example.lets_play.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility class for JWT (JSON Web Token) operations including generation, validation, and parsing.
 * 
 * <p>This class provides comprehensive JWT functionality for the authentication system, including
 * token generation from authentication objects, token validation with proper error handling,
 * and secure claim extraction. It uses HMAC-SHA512 for token signing and supports configurable
 * expiration times.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li>Secure token generation with HMAC-SHA512 algorithm</li>
 *   <li>Comprehensive token validation with detailed error logging</li>
 *   <li>Safe claim extraction with proper exception handling</li>
 *   <li>Configurable secret key and expiration time via application properties</li>
 * </ul></p>
 * 
 * @apiNote JWT tokens are stateless and contain user identification information
 * @implNote Uses JJWT library version 0.11.5 for JWT operations
 * @security Secret key must be at least 256 bits for HMAC-SHA512 algorithm
 * 
 * @author Zone01 Developer
 * @version 1.0
 * @since 2024
 */
@Component
public class JwtUtils {
    /**
     * Logger instance for JWT-related operations and error reporting.
     */
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    /**
     * JWT secret key loaded from application properties.
     * 
     * @implNote Must be at least 256 bits (32 characters) for HMAC-SHA512
     * @security Should be kept secret and rotated regularly in production
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * JWT token expiration time in milliseconds loaded from application properties.
     * 
     * @apiNote Default is typically 24 hours (86400000ms)
     */
    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Generates a secure signing key from the configured secret.
     * 
     * <p>This method creates a SecretKey instance using HMAC-SHA algorithm suitable
     * for JWT token signing. The key is generated from the configured secret string.</p>
     * 
     * @return SecretKey instance for JWT signing operations
     * 
     * @implNote Uses JJWT's Keys.hmacShaKeyFor() for secure key generation
     * @security Key strength depends on the length and entropy of the secret string
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generates a JWT token from an authenticated user principal.
     * 
     * <p>This method creates a signed JWT token containing the user's ID as the subject,
     * issued at the current time, and with a configured expiration time. The token is
     * signed using HMAC-SHA512 algorithm for maximum security.</p>
     * 
     * @param authentication the Spring Security authentication object containing user details
     * @return String the compact JWT token ready for use in Authorization headers
     * 
     * @throws ClassCastException if the authentication principal is not a UserPrincipal
     * 
     * @apiNote The generated token should be prefixed with "Bearer " in Authorization headers
     * @implNote Token subject contains the user ID for efficient user lookup
     * @security Token is signed with HMAC-SHA512 and includes expiration time
     */
    public String generateJwtToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getId()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extracts the user ID from a valid JWT token.
     * 
     * <p>This method parses and validates the JWT token, then extracts the user ID
     * from the token's subject claim. The token signature is verified during parsing.</p>
     * 
     * @param token the JWT token to parse (without "Bearer " prefix)
     * @return String the user ID extracted from the token subject
     * 
     * @throws JwtException if the token is invalid, expired, or malformed
     * 
     * @apiNote Token must be valid and non-expired for successful extraction
     * @implNote Performs full token validation including signature verification
     * @security Only tokens with valid signatures can be successfully parsed
     */
    public String getUserIdFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validates a JWT token for authenticity, integrity, and expiration.
     * 
     * <p>This method performs comprehensive validation of JWT tokens including signature
     * verification, expiration checking, and format validation. All validation errors
     * are logged with appropriate detail levels for debugging and security monitoring.</p>
     * 
     * @param authToken the JWT token to validate (without "Bearer " prefix)
     * @return boolean true if the token is valid, false otherwise
     * 
     * @apiNote This method never throws exceptions - all errors result in false return value
     * @implNote Comprehensive error logging helps with debugging authentication issues
     * @security Failed validations are logged for security monitoring purposes
     * 
     * @see SecurityException for invalid JWT signatures
     * @see MalformedJwtException for malformed JWT tokens
     * @see ExpiredJwtException for expired JWT tokens
     * @see UnsupportedJwtException for unsupported JWT features
     * @see IllegalArgumentException for empty or null token claims
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}