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
 * JWT utilities for token generation, parsing and validation.
 *
 * <p>Uses HMAC-SHA512 for signing and supports configurable
 * expiration and secrets.</p>
 */
@Component
public class JwtUtils {
    /**
     * Logger instance for JWT-related operations and error reporting.
     */
    private static final Logger LOGGER =
        LoggerFactory.getLogger(JwtUtils.class);
    /** Minimum HMAC key size in bits required for HS512. */
    private static final int HMAC_MIN_BITS = 512;

    /** Number of bits per byte. Used to compute key length in bits. */
    private static final int BITS_PER_BYTE = 8;

    /**
     * Message digest algorithm used to expand short secrets into a
     * fixed-length key.
     */
    private static final String HASH_ALGO = "SHA-512";

    /** JWT secret key loaded from application properties. */
    // Prefer environment variable APP_JWT_SECRET. Fall back to
    // the app.jwt.secret property when not present.
    @Value("${APP_JWT_SECRET:${app.jwt.secret}}")
    private String jwtSecret;

    /**
     * JWT token expiration time in milliseconds from application
     * properties.
     */
    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Generates a SecretKey suitable for HMAC-SHA signing from the
     * configured secret string.
     *
     * @return a SecretKey derived from the configured JWT secret suitable
     *         for HMAC-SHA signing (HS512)
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

    // Ensure key is large enough for HS512 (>= 512 bits). If the
    // provided secret is too short, derive a 512-bit key by hashing
    // the secret with SHA-512.
        if (keyBytes.length * BITS_PER_BYTE < HMAC_MIN_BITS) {
            try {
                final String algo = HASH_ALGO;
                final MessageDigest digest = MessageDigest.getInstance(algo);
                keyBytes = digest.digest(keyBytes);
            } catch (NoSuchAlgorithmException e) {
                // SHA-512 must be available on the platform; rethrow as runtime
                // if not
                final String errSuffix = " MessageDigest not available";
                final String err = HASH_ALGO + errSuffix;
                throw new IllegalStateException(err, e);
            }
        }
        // Use JJWT helper to produce an HMAC-SHA key of the correct length.
        final SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);

        return signingKey;
    }

    /**
     * Generates a signed JWT token for the authenticated principal.
     *
    * @param authentication Spring Security authentication
    *        containing user details
     * @return compact JWT token (no "Bearer " prefix)
     */
    public String generateJwtToken(final Authentication authentication) {
    // Use the AppUserPrincipal interface to avoid tight coupling to
    // a concrete implementation.
    final AppUserPrincipal userPrincipal;
    userPrincipal = (AppUserPrincipal) authentication.getPrincipal();

    final long now = System.currentTimeMillis();
    final Date issuedAt = new Date(now);
    final Date expiryDate = new Date(now + jwtExpirationMs);

    return Jwts.builder()
        .setSubject(userPrincipal.getId())
        .setIssuedAt(issuedAt)
        .setExpiration(expiryDate)
        .signWith(
            getSigningKey(),
            SignatureAlgorithm.HS512
        )
        .compact();
    }

    /**
     * Extract the user id (subject) from a JWT token.
     *
     * @param token compact JWT token (no "Bearer " prefix)
     * @return user id from token subject
     */
    public String getUserIdFromJwtToken(final String token) {
    final var parserBuilder = Jwts.parserBuilder();
    parserBuilder.setSigningKey(getSigningKey());

    final var parser = parserBuilder.build();

    return parser
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
    }

    /**
     * Validate a JWT token string.
     *
     * @param authToken compact JWT token (no "Bearer " prefix)
     * @return true when token is valid, false otherwise
     */
    public boolean validateJwtToken(final String authToken) {
        try {
        final var builder = Jwts.parserBuilder();
        builder.setSigningKey(getSigningKey());
        final var parser = builder.build();

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
