package com.ninjaone.dundie_awards.util;

import com.ninjaone.dundie_awards.config.JwtTokenProvider;
import com.ninjaone.dundie_awards.model.Role;
import com.ninjaone.dundie_awards.model.User;
import com.ninjaone.dundie_awards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for test authentication.
 * Creates test users and generates JWT tokens for testing.
 */
@Component
public class TestAuthUtil {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Creates a test user if it doesn't exist and returns a JWT token.
     *
     * @param username the username
     * @param password the password
     * @param roles the roles for the user
     * @return JWT token as a Bearer token string
     */
    public String getAuthToken(String username, String password, Set<Role> roles) {
        // Create or get user
        @SuppressWarnings("null") // userRepository.save() never returns null in practice
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(username)
                            .password(passwordEncoder.encode(password))
                            .roles(roles)
                            .enabled(true)
                            .build();
                    return userRepository.save(newUser);
                });

        // Create authentication
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toSet());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                authorities
        );

        // Generate token
        return "Bearer " + jwtTokenProvider.generateToken(authentication);
    }

    /**
     * Creates a test user with ROLE_USER and returns a JWT token.
     *
     * @return JWT token as a Bearer token string
     */
    public String getDefaultAuthToken() {
        return getAuthToken("testuser", "password", Set.of(Role.ROLE_USER));
    }

    /**
     * Creates a test admin user with ROLE_ADMIN and returns a JWT token.
     *
     * @return JWT token as a Bearer token string
     */
    public String getAdminAuthToken() {
        return getAuthToken("admin", "admin", Set.of(Role.ROLE_ADMIN));
    }
}

