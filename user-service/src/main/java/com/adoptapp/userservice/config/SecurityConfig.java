package com.adoptapp.userservice.config;

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
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new JsonAuthenticationEntryPoint())
                        .accessDeniedHandler(new JsonAccessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/users", "/users/by-id/**").hasAnyRole("ADOPTER", "VOLUNTEER", "VET", "SHELTER_ADMIN", "ADMIN")
                        .requestMatchers("/users/by-email/{email}/auth").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/by-id/{id}/history").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/users").hasAnyRole("ADOPTER", "SHELTER_ADMIN", "VOLUNTEER", "VET", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/by-id/**").hasAnyRole("ADOPTER", "SHELTER_ADMIN", "VOLUNTEER", "VET", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/by-id/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/doc/swagger-ui.html",
                                "/doc/swagger-ui/**"
                        ).permitAll()
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
