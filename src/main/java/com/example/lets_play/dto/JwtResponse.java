package com.example.lets_play.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

/**
 * Authentication response containing a JWT access token and basic
 * user information.
 *
 * <p>After successful authentication, the server returns this DTO. Clients
 * must use the {@code token} value and include it in the
 * {@code Authorization} header for subsequent requests:
 * {@code Authorization: Bearer &lt;token&gt;}.</p>
 *
 * <p><strong>Security:</strong> Treat the token as a secret; do not log it or
 * expose it in URLs. Tokens should be stored securely on the client
 * (for example, in an HTTP-only cookie or secure platform storage).</p>
 *
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class JwtResponse {
    /** JWT access token value. */
    private String token;

    /** Token type; default is "Bearer". */
    private String type = "Bearer";

    /** User identifier. */
    private String id;

    /** User display name. */
    private String name;

    /** User email. */
    private String email;

    /** User role (for example, "USER" or "ADMIN"). */
    private String role;

    /**
     * Convenience constructor used by controllers to build a JWT response.
     *
    * @param accessToken the JWT token string
    * @param userId the user identifier
    * @param userName the user display name
    * @param userEmail the user email address
    * @param userRole the user's role
     */
    public JwtResponse(
            final String accessToken,
            final String userId,
            final String userName,
            final String userEmail,
            final String userRole
    ) {
        this.token = accessToken;
        this.id = userId;
        this.name = userName;
        this.email = userEmail;
        this.role = userRole;
    }
}
