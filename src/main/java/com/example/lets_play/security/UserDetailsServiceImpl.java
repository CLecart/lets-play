package com.example.lets_play.security;

import com.example.lets_play.model.User;
import com.example.lets_play.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service used by Spring Security to load user-specific data during
 * authentication.
 *
 * <p>This implementation supports loading users either by email (username)
 * or by unique ID. It converts {@link com.example.lets_play.model.User}
 * entities into {@link com.example.lets_play.security.UserPrincipal}
 * instances understood by Spring Security.</p>
 *
 * @since 1.0
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    /** Repository used to lookup users by email or id. */
    @Autowired
    private UserRepository userRepository;

    /**
     * Load a user by their email address.
     *
     * <p>Intended for use by Spring Security during authentication. Subclasses
     * may override but should preserve the contract: throw
     * {@link UsernameNotFoundException} when no user is found.</p>
     *
     * @param email the user's email used as the username
     * @return a Spring Security compatible UserDetails instance
     * @throws UsernameNotFoundException when the user cannot be found
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String email)
        throws UsernameNotFoundException {
        final String msg = "User Not Found with email: " + email;
        final User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(msg));

        return UserPrincipal.create(user);
    }

    /**
     * Load a user by unique id.
     *
     * <p>This method is used when a request contains a user identifier
     * (for example extracted from a JWT). Subclasses may override but must
     * throw {@link UsernameNotFoundException} when the id is unknown.</p>
     *
     * @param id the unique user id
     * @return a Spring Security compatible UserDetails instance
     * @throws UsernameNotFoundException when the user cannot be found
     */
    @Transactional
    public UserDetails loadUserById(final String id) {
        final String msg = "User Not Found with id: " + id;
        final User user = userRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException(msg));

        return UserPrincipal.create(user);
    }
}
