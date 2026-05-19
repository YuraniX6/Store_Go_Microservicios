package com.storego.profileservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("No Bearer token encontrado en Authorization header");
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);
            log.debug("Token encontrado, validando...");

            Claims claims = jwtService.parseToken(token);
            String sub = claims.getSubject();

            UUID userId;
            try {
                userId = UUID.fromString(sub);
            } catch (IllegalArgumentException ex) {
                log.warn("Subject '{}' no es un UUID válido", sub);
                filterChain.doFilter(request, response);
                return;
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            Object rolesObj = claims.get("roles");
            if (rolesObj instanceof List<?> roleList) {
                for (Object r : roleList) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + r));
                }
            } else if (rolesObj != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + rolesObj));
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId.toString(),
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Usuario {} autenticado con authorities {}", userId, authorities);

        } catch (JwtException ex) {
            log.warn("JWT inválido o expirado: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception ex) {
            log.error("Error al procesar JWT: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
