package com.qrave.qrservice.config;

import com.qrave.qrservice.repository.InvalidatedTokenRepository;
import com.qrave.qrservice.repository.UserRepository;
import com.qrave.qrservice.security.JwtAuthenticationFilter;
import com.qrave.qrservice.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    public SecurityConfig(JwtService jwtService,
                          UserRepository userRepository,
                          InvalidatedTokenRepository invalidatedTokenRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.invalidatedTokenRepository = invalidatedTokenRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(
                jwtService,
                userRepository,
                invalidatedTokenRepository
        );

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 👈 Habilita CORS
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/qr/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:8100")); // 👈 Cambia por tu frontend si aplica
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
