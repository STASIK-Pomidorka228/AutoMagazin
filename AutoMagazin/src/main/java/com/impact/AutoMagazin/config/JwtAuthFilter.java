package com.impact.AutoMagazin.config;

import com.impact.AutoMagazin.models.UserCredentials;
import com.impact.AutoMagazin.repositories.CredentialsRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JwtAuthFilter {

    private final JwtService jwtService;
    private final CredentialsRepository credentialsRepository;

    public JwtAuthFilter(JwtService jwtService,
                         CredentialsRepository credentialsRepository) {
        this.jwtService = jwtService;
        this.credentialsRepository = credentialsRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username = null;
        try {
            username = jwtService.extractUsername(token);
        } catch (Exception ignored) {
        }

        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            UserCredentials credentials =
                    (UserCredentials) credentialsRepository.findByUsername(username).orElse(null);

            if (credentials != null && jwtService.isTokenValid(token)) {

                String role = jwtService.extractRole(token);

                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
                );

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                authorities
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
}
