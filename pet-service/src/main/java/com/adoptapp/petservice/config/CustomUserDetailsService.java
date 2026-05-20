package com.adoptapp.petservice.config;

import com.adoptapp.petservice.client.UserServiceClient;
import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    public CustomUserDetailsService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            ResponseEntity<UserAuthResponse> response = userServiceClient.getUserAuthByEmail(email);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Usuario no encontrado: {}", email);
                throw new UsernameNotFoundException("Usuario no encontrado: " + email);
            }

            UserAuthResponse user = response.getBody();
            if (!user.enabled()) {
                throw new UsernameNotFoundException("Usuario deshabilitado: " + email);
            }
            return User.withUsername(user.email())
                    .password(user.password())
                    .roles(user.role())
                    .build();
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al autenticar usuario {}: {}", email, e.getMessage());
            throw new UsernameNotFoundException("Error al autenticar: " + email);
        }
    }
}
