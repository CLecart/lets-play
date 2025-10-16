package com.example.lets_play.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Utility class for JWT operations: generation, validation, and parsing.
 *
 * <p>Provides JWT generation from Authentication objects, validation with
 * detailed error handling, and secure claim extraction. Uses HMAC-SHA512
 * for signing and supports configurable expiration.</p>
 *
 * <p>Features include secure signing, comprehensive validation, safe claim
 * extraction, and configurable secrets/expiration via properties.</p>
 *
 * <p><strong>API Note:</strong> JWT tokens are stateless and contain user id
 * information.</p>
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
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtils.class);
    /** Minimum HMAC key size in bits required for HS512. */
    private static final int HMAC_MIN_BITS = 512;

    /** Number of bits per byte. Used to compute key length in bits. */
    private static final int BITS_PER_BYTE = 8;

    /**
     * Message digest algorithm used to expand short secrets into a
     * fixed-length key.
     */
    private static final String HASH_ALGO = "SHA-512";

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
     * <p>This method creates a SecretKey instance using an HMAC-SHA algorithm
     * suitable for JWT token signing. The key is generated from the configured
     * secret string.</p>
     *
     * @return SecretKey instance for JWT signing operations
     *
     * <p><strong>Implementation Note:</strong> Uses JJWT's
     * Keys.hmacShaKeyFor() for secure key generation.</p>
     * <p><strong>Security:</strong> Key strength depends on the length and
     * entropy of the secret string.</p>
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        // Ensure key is large enough for HS512 (>= 512 bits). If the provided
        // secret is too short, derive a 512-bit key by hashing the secret
        // with SHA-512.
        if (keyBytes.length * BITS_PER_BYTE < HMAC_MIN_BITS) {
            try {
                final MessageDigest digest = MessageDigest.getInstance(HASH_ALGO);
                keyBytes = digest.digest(keyBytes);
            } catch (NoSuchAlgorithmException e) {
                // SHA-512 must be available on the platform; rethrow as runtime if not
                final String err = HASH_ALGO + " MessageDigest not available";
                throw new IllegalStateException(err, e);
            }
        }

        // Use JJWT helper to produce an HMAC-SHA key of the correct length.
        final SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);
        return signingKey;
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
    public String generateJwtToken(final Authentication authentication) {
        // Use the AppUserPrincipal interface to avoid tight coupling to concrete implementation
    final AppUserPrincipal userPrincipal = (AppUserPrincipal) authentication.getPrincipal();

    final long now = System.currentTimeMillis();
    final Date issuedAt = new Date(now);
    final Date expiryDate = new Date(now + jwtExpirationMs);

    return Jwts.builder()
        .setSubject(userPrincipal.getId())
        .setIssuedAt(issuedAt)
        .setExpiration(expiryDate)
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
    public String getUserIdFromJwtToken(final String token) {
        final var builder = Jwts.parserBuilder();
        builder.setSigningKey(getSigningKey());

        final var parser = builder.build();

        return parser.parseClaimsJws(token)
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
    public boolean validateJwtToken(final String authToken) {
        try {
            final var parser = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build();

            parser.parseClaimsJws(authToken);
            return true;
        } catch (SecurityException e) {
            LOGGER.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            LOGGER.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            LOGGER.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOGGER.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
