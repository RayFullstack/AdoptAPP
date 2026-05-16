package com.adoptapp.petservice.config;

import com.adoptapp.petservice.model.Pet;
import com.adoptapp.petservice.repository.PetRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class PetSecurity {
    private final PetRepository petRepository;

    public PetSecurity(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public boolean canEdit(Long petId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if (hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }

        Pet target = petRepository.findById(petId).orElse(null);
        if (target == null) {
            return false;
        }

        return hasRole(authentication, "ROLE_SHELTER");
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
