package com.example.lets_play.security;

import com.example.lets_play.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
// org.springframework.security.core.userdetails.UserDetails import not required here
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.Collections;

/**
 * Security principal representing an authenticated user for Spring Security.
 *
 * <p>This class adapts the domain {@link com.example.lets_play.model.User} to
 * Spring Security's {@link org.springframework.security.core.userdetails.UserDetails}
 * interface. It contains only the information necessary for authentication and
 * authorization decisions (id, username/email, authorities).</p>
 *
 * <p><strong>Security:</strong> The password field here contains the hashed password
 * from persistence and must never be serialized or logged.</p>
 *
 * @since 1.0
 */
@AllArgsConstructor
public class UserPrincipal implements AppUserPrincipal {
    private String id;
    private String name;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole())
        );

        return new UserPrincipal(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPassword(),
            authorities
        );
    }

    public String getId() {
        return id;
    }

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