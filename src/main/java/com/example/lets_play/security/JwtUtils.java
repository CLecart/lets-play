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
 * </ul>
 * 
 * <p><strong>API Note:</strong> JWT tokens are stateless and contain user identification information</p>
 * <p><strong>Implementation Note:</strong> Uses JJWT library version 0.11.5 for JWT operations</p>
 * <p><strong>Security:</strong> Secret key must be at least 256 bits for HMAC-SHA512 algorithm</p>
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
     * <p><strong>Implementation Note:</strong> Must be at least 256 bits (32 characters) for HMAC-SHA512</p>
     * <p><strong>Security:</strong> Should be kept secret and rotated regularly in production</p>
     */
    // Prefer environment variable APP_JWT_SECRET, fall back to app.jwt.secret property
    @Value("${APP_JWT_SECRET:${app.jwt.secret}}")
    private String jwtSecret;

    /**
     * JWT token expiration time in milliseconds loaded from application properties.
     * 
     * <p><strong>API Note:</strong> Default is typically 24 hours (86400000ms)</p>
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
     * <p><strong>Implementation Note:</strong> Uses JJWT's Keys.hmacShaKeyFor() for secure key generation</p>
     * <p><strong>Security:</strong> Key strength depends on the length and entropy of the secret string</p>
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
     * <p><strong>API Note:</strong> The generated token should be prefixed with "Bearer " in Authorization headers</p>
     * <p><strong>Implementation Note:</strong> Token subject contains the user ID for efficient user lookup</p>
     * <p><strong>Security:</strong> Token is signed with HMAC-SHA512 and includes expiration time</p>
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
     * <p><strong>API Note:</strong> Token must be valid and non-expired for successful extraction</p>
     * <p><strong>Implementation Note:</strong> Performs full token validation including signature verification</p>
     * <p><strong>Security:</strong> Only tokens with valid signatures can be successfully parsed</p>
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
     * <p><strong>API Note:</strong> This method never throws exceptions - all errors result in false return value</p>
     * <p><strong>Implementation Note:</strong> Comprehensive error logging helps with debugging authentication issues</p>
     * <p><strong>Security:</strong> Failed validations are logged for security monitoring purposes</p>
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