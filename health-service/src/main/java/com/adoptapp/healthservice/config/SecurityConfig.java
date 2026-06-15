package com.adoptapp.healthservice.config;

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
                        .requestMatchers(HttpMethod.GET, "/health", "/health/by-id/**").hasAnyRole("VET", "SHELTER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/health/by-id/*/history").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/health").hasAnyRole("SHELTER_ADMIN", "ADMIN", "VET")
                        .requestMatchers(HttpMethod.PUT, "/health/by-id/*").hasAnyRole("SHELTER_ADMIN", "ADMIN", "VET")
                        .requestMatchers(HttpMethod.DELETE, "/health/by-id/*").hasAnyRole("ADMIN", "SHELTER_ADMIN", "VET")
                        .requestMatchers(HttpMethod.GET, "/health/by-pet/*").hasAnyRole("VET", "SHELTER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/health/by-pet/*").hasAnyRole("ADMIN", "SHELTER_ADMIN", "VET")
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
