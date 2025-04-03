package com.qrave.qrservice.security;

import com.qrave.qrservice.repository.InvalidatedTokenRepository;
import com.qrave.qrservice.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter implements Filter {

    private final JwtService jwtService;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository, InvalidatedTokenRepository invalidatedTokenRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.invalidatedTokenRepository = invalidatedTokenRepository;
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Permitir preflight de CORS
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (!jwtService.isTokenValid(token) || invalidatedTokenRepository.existsByToken(token)) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
                return;
            }

            var authentication = jwtService.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }
}
