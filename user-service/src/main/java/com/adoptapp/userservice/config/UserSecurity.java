package com.adoptapp.userservice.config;

import com.adoptapp.userservice.model.User;
import com.adoptapp.userservice.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    private final UserRepository userRepository;

    public UserSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean canEdit(Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if (hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }

        User target = userRepository.findById(userId).orElse(null);
        if (target == null) {
            return false;
        }

        if (hasRole(authentication, "ROLE_SHELTER_ADMIN") && target.getRole() == User.Role.VOLUNTEER) {
            return true;
        }

        String email = authentication.getName();

        return target.getEmail().equalsIgnoreCase(email);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
