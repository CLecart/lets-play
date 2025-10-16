package com.example.lets_play.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.example.lets_play.config.AppConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for creating a new user.
 *
 * <p>Contains only the fields required to create a user in the system.
 * Validation constraints are applied via Jakarta Bean Validation
 * annotations. Passwords provided here must meet the minimum security
 * requirements and will be encoded before persistence.</p>
 *
 * <p>
 * <strong>Note:</strong> The {@code role} field defaults to {@code "USER"}
 * when omitted.
 * </p>
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    /** User display name. */
    @NotBlank(message = "Name is required")
    @Size(
        min = AppConstants.USER_NAME_MIN,
        max = AppConstants.USER_NAME_MAX,
        message = "Name must be between 2 and 50 characters"
    )
    private String name;

    /** User email address. */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    /** Plain-text password supplied at creation; encoded before persistence. */
    @NotBlank(message = "Password is required")
    @Size(
        min = AppConstants.USER_PASSWORD_MIN,
        message = "Password must be at least 6 characters"
    )
    private String password;

    /** Role assigned to the new user (defaults to USER). */
    private String role = "USER";
}
