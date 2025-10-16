package com.example.lets_play.dto;

import jakarta.validation.constraints.Size;
import com.example.lets_play.config.AppConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for updating an existing user.
 *
 * <p>All fields are optional — only provided fields will be applied during an
 * update operation. Role changes are restricted to administrators at the service
 * layer and should be validated before applying.</p>
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    /** Optional new display name for the user. */
    @Size(min = AppConstants.USER_NAME_MIN, max = AppConstants.USER_NAME_MAX,
        message = "Name must be between 2 and 50 characters")
    private String name;

    /** Optional new password; must meet minimum length. */
    @Size(min = AppConstants.USER_PASSWORD_MIN, message = "Password must be at least 6 characters")
    private String password;

    /** Optional role change (validate at service layer). */
    private String role;
}
