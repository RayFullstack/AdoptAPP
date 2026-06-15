package com.adoptapp.supplyservice.config;

import com.adoptapp.sharedkernel.security.JsonAccessDeniedHandler;
import com.adoptapp.sharedkernel.security.JsonAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/supplies/internal/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/supplies/by-id/{id}/history").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/supplies/**").hasAnyRole("ADOPTER", "VOLUNTEER", "VET", "SHELTER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/supplies/**").hasAnyRole("ADMIN", "SHELTER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/supplies/**").hasAnyRole("ADMIN", "SHELTER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/supplies/**").hasAnyRole("ADMIN", "SHELTER_ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
