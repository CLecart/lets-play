package com.example.lets_play.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

/**
 * Authentication response containing a JWT access token and basic user information.
 *
 * <p>After successful authentication, the server returns this DTO. Clients must
 * use the {@code token} value and include it in the {@code Authorization} header
 * for subsequent requests: {@code Authorization: Bearer &lt;token&gt;}.</p>
 *
 * <p><strong>Security:</strong> Treat the token as a secret; do not log it or expose it
 * in URLs. Tokens should be stored securely on the client (for example, an HTTP-only cookie
 * or secure storage provided by the client platform).</p>
 *
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String id;
    private String name;
    private String email;
    private String role;

    public JwtResponse(String accessToken, String id, String name, String email, String role) {
        this.token = accessToken;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
