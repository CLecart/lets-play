package com.example.lets_play.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for creating a new user.
 *
 * <p>Contains only the fields required to create a user in the system. Validation
 * constraints are applied via Jakarta Bean Validation annotations. Passwords provided
 * here must meet the minimum security requirements and will be encoded before
 * persistence.</p>
 *
 * <p><strong>Note:</strong> The {@code role} field defaults to {@code "USER"} when omitted.</p>
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    private String role = "USER";
}