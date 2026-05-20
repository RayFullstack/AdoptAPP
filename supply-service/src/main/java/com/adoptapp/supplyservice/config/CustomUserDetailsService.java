package com.adoptapp.supplyservice.config;

import com.adoptapp.supplyservice.client.UserServiceClient;
import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ResponseEntity<UserAuthResponse> response = userServiceClient.getUserAuthByEmail(email);

        if (response == null || !response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new UsernameNotFoundException("Usuario no encontrado con email: " + email);
        }

        UserAuthResponse user = response.getBody();

        if (!user.enabled()) {
            throw new UsernameNotFoundException("Usuario deshabilitado: " + email);
        }

        String role = user.role() != null ? user.role() : "ADOPTER";

        List<org.springframework.security.core.GrantedAuthority> authorities = List.of(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)
        );

        return new org.springframework.security.core.userdetails.User(
                user.email(),
                user.password(),
                user.enabled(),
                true, true, true,
                authorities
        );
    }
}
