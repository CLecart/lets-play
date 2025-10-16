package com.example.lets_play.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.example.lets_play.config.AppConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    /** Database identifier for the user (MongoDB ObjectId as string). */
    @Id
    private String id;

    /** The user's full name. */
    @NotBlank(message = "Name is required")
    @Size(min = AppConstants.USER_NAME_MIN,
          max = AppConstants.USER_NAME_MAX,
          message = "Name must be between 2 and 50 characters")
    private String name;

    /** The user's email address; must be unique. */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Indexed(unique = true)
    private String email;

    /** The user's password (WRITE_ONLY in JSON). */
    @NotBlank(message = "Password is required")
    @Size(min = AppConstants.USER_PASSWORD_MIN,
        message = "Password must be at least 6 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private String password;

    /**
     * The user's role. Defaults to 'USER'. Use values from AppConstants where
     * appropriate.
     */
    @NotBlank(message = "Role is required")
    private String role = "USER";

    /**
     * Convenience constructor for creating a user instance.
     *
     * @param nameParam the user's full name
     * @param emailParam the user's email address
     * @param passwordParam the user's password
     * @param roleParam the user's role
     */
    public User(final String nameParam,
                final String emailParam,
                final String passwordParam,
                final String roleParam) {
        this.name = nameParam;
        this.email = emailParam;
        this.password = passwordParam;
        this.role = roleParam;
    }
}
