package com.example.lets_play.security;

import com.example.lets_play.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
// The UserDetails import is intentionally omitted.
// This class adapts the domain User to Spring Security's UserDetails
// externally and does not directly implement that framework type here.
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.Collections;

/**
 * Security principal representing an authenticated user for Spring Security.
 *
 * <p>
 * This class adapts the domain {@link com.example.lets_play.model.User} to
 * Spring Security's
 * {@link org.springframework.security.core.userdetails.UserDetails}
 * interface. It contains only the information necessary for authentication
 * and authorization decisions (id, username/email, authorities).
 * </p>
 *
 * <p>
 * <strong>Security:</strong> The password field here contains the hashed
 * password from persistence and must never be serialized or logged.
 * </p>
 *
 * @since 1.0
 */
@AllArgsConstructor
public final class UserPrincipal implements AppUserPrincipal {
    /** Persistent identifier of the user (string form of Mongo id). */
    private final String id;

    /** Display name for the user. */
    private final String name;

    /** Email address used as the username for authentication. */
    private final String email;

    /** Hashed user password from persistence; never serialize. */
    private final String password;

    /** Granted authorities used for authorization checks. */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Create a UserPrincipal from the domain {@code User}.
     *
     * @param user the domain user (must not be null)
     * @return a new UserPrincipal with authorities derived from the user's role
     */
    public static UserPrincipal create(final User user) {
        final String role = "ROLE_" + user.getRole();
        final SimpleGrantedAuthority authority =
            new SimpleGrantedAuthority(role);
        final Collection<GrantedAuthority> authorities = Collections
            .singletonList(authority);

        return new UserPrincipal(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPassword(),
            authorities
        );
    }

    /** @return the persistent identifier of the user */
    public String getId() {
        return id;
    }

    /** @return the display name of the user */
    public String getName() {
        return name;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
