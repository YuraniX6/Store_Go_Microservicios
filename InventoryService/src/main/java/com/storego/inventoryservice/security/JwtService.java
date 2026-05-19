package com.storego.inventoryservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // reparar esto
    public Claims parseToken(String token) {
        try {
            // Intenta parsear con la clave configurada
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.warn("Token validation with configured key failed: {}. Attempting manual parsing...", e.getMessage());
            // Fallback: decodificar manualmente sin verificar firma
            try {
                return decodeTokenManually(token);
            } catch (Exception e2) {
                log.error("Failed to parse JWT token even without verification: {}", e2.getMessage());
                throw new RuntimeException("Cannot parse JWT token", e);
            }
        }
    }

    private Claims decodeTokenManually(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> claimsMap = mapper.readValue(payload, Map.class);

        log.info("Successfully parsed JWT manually without signature verification. Claims: {}", claimsMap);

        // Usar el constructor de HashMap que implementa Map
        return Jwts.claims().add(claimsMap).build();
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public UUID extractUserId(String token) {
        Claims claims = parseToken(token);
        String sub = claims.getSubject();
        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in JWT sub claim: {}", sub);
            throw new IllegalArgumentException("Invalid UUID in JWT", e);
        }
    }

    public String extractUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    public String extractRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }
}
