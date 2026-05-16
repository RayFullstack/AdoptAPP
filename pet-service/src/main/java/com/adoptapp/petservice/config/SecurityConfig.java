package com.adoptapp.petservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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
                        .requestMatchers(HttpMethod.POST, "/pets").hasAnyRole("ADOPTER", "SHELTER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/pets/by-id/**").hasAnyRole("ADOPTER", "SHELTER", "ADMIN")
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

    @Bean
    public UserDetailsService userDetailsService() {
        var admin = User.withUsername("admin@empresa.com")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN")
                .build();
        var shelter = User.withUsername("carlos.lopez@empresa.com")
                .password(passwordEncoder().encode("admin123"))
                .roles("SHELTER")
                .build();
        var adopter = User.withUsername("ana.garcia@empresa.com")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADOPTER")
                .build();
        return new InMemoryUserDetailsManager(admin, shelter, adopter);
    }
}
