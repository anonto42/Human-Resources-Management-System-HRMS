package com.hrmf.hrms_backend.config;

import com.hrmf.hrms_backend.enums.UserRole;
import com.hrmf.hrms_backend.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/verify-otp",
                                "/api/v1/auth/reset-password",
                                "/api/v1/auth/refresh-token",
                                "/api/v1/files/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()

                        // Super Admin endpoints
                        .requestMatchers("/api/v1/super-admin/**")
                        .hasAuthority(UserRole.SUPER_ADMIN.name())

                        // Sub-Admin endpoints
                        .requestMatchers("/api/v1/sub-admin/**")
                        .hasAnyAuthority(
                                UserRole.SUPER_ADMIN.name(),
                                UserRole.SUB_ADMIN.name()
                        )

                        // Employer endpoints
                        .requestMatchers("/api/v1/employer/**")
                        .hasAnyAuthority(
                                UserRole.SUPER_ADMIN.name(),
                                UserRole.SUB_ADMIN.name(),
                                UserRole.EMPLOYER.name()
                        )

                        // Employee endpoints
                        .requestMatchers("/api/v1/employee/**")
                        .hasAnyAuthority(
                                UserRole.SUPER_ADMIN.name(),
                                UserRole.SUB_ADMIN.name(),
                                UserRole.EMPLOYER.name(),
                                UserRole.EMPLOYEE.name()
                        )

                        .requestMatchers(
                                "/api/v1/auth/logout",
                                "/api/v1/auth/change-password",
                                "/api/v1/users/me/**"
                        )
                        .hasAnyAuthority(
                                UserRole.SUPER_ADMIN.name(),
                                UserRole.SUB_ADMIN.name(),
                                UserRole.EMPLOYER.name(),
                                UserRole.EMPLOYEE.name()
                        )

                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}