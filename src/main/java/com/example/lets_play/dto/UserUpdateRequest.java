package com.example.lets_play.dto;

import jakarta.validation.constraints.Size;
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

    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String role;
}
