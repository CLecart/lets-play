package com.example.lets_play.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO returned to clients representing a user without sensitive fields.
 *
 * <p>This class is used as the canonical representation of a user in API
 * responses. It intentionally omits sensitive data such as passwords and
 * security details. Use {@link com.example.lets_play.dto.UserCreateRequest}
 * and {@link com.example.lets_play.dto.UserUpdateRequest} for incoming
 * requests when creating or updating users.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * UserResponse resp = new UserResponse(user.getId(), user.getName(),
 *     user.getEmail(), user.getRole());
 * </pre>
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    /** User identifier. */
    private String id;

    /** User display name. */
    private String name;

    /** User email. */
    private String email;

    /** User role (for example "USER"). */
    private String role;
}
