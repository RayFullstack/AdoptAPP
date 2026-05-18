package com.adoptapp.petservice.config;

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
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/pets", "/pets/by-id/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/pets/by-id/{id}/history").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/pets").hasAnyRole("ADOPTER", "SHELTER_ADMIN", "VOLUNTEER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/pets/by-id/**").hasAnyRole("SHELTER_ADMIN", "VOLUNTEER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/pets/by-id/**").hasRole("ADMIN")
                        .requestMatchers("/pets/**").hasRole("ADMIN")
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
