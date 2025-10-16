package com.example.lets_play.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.example.lets_play.config.AppConstants;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for user authentication login requests.
 *
 * <p>This class encapsulates the credentials required for user authentication,
 * including email and password validation constraints to ensure data integrity
 * and security compliance.</p>
 *
 * <p>The class uses Bean Validation (JSR-303) annotations to enforce
 * input validation rules and Lombok annotations to reduce boilerplate
 * code.</p>
 *
 * @author Let's Play Development Team
 * @version 1.0
 * @since 1.0
 *
 * @see com.example.lets_play.controller.AuthController
 * @see com.example.lets_play.controller.AuthController
 */
@Data
public class LoginRequest {

    /**
     * User's email address used for authentication.
     *
     * <p>Must be a valid email format and cannot be blank. It is used as the
     * primary identifier for user authentication.</p>
     *
     * <p><strong>API Note:</strong> Email format validation is performed using
     * Jakarta Bean Validation.</p>
     */
    @NotBlank(message = "Email is required and cannot be blank")
    @Email(message = "Email must be a valid email address format")
    @Size(
        max = AppConstants.EMAIL_MAX,
        message = "Email must not exceed 100 characters"
    )
    private String email;

    /**
     * User's password for authentication.
     *
    * <p>Must meet minimum security requirements and cannot be blank.
    * The password will be validated against the stored hashed
    * password.</p>
    *
    * <p><strong>API Note:</strong> Password is transmitted in plain text
    * but should always be sent over HTTPS.</p>
    * <p><strong>Implementation Note:</strong> Password is never stored in
    * plain text in the database.</p>
    * <p><strong>Security:</strong> Ensure this field is not logged or
    * exposed in error messages.</p>
     */
    @NotBlank(message = "Password is required and cannot be blank")
    @Size(
        min = AppConstants.USER_PASSWORD_MIN,
        max = AppConstants.USER_PASSWORD_MAX,
        message = "Password must be between 6 and 100 characters"
    )
    private String password;
}
