package com.storego.profileservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;

    public Claims parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            log.warn("Token expirado: {}", ex.getMessage());
            throw ex;
        } catch (SignatureException ex) {
            log.warn("Firma inválida del token: {}", ex.getMessage());
            throw ex;
        } catch (MalformedJwtException ex) {
            log.warn("Token malformado: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.warn("Error al parsear token: {}", ex.getMessage());
            throw ex;
        }
    }

    public UUID extractSubjectAsUUID(String token) {
        try {
            Claims claims = parseToken(token);
            String sub = claims.getSubject();
            return UUID.fromString(sub);
        } catch (IllegalArgumentException ex) {
            log.warn("Subject no es un UUID válido: {}", ex.getMessage());
            throw ex;
        }
    }

    public String extractRole(String token) {
        Claims claims = parseToken(token);
        Object role = claims.get("role");
        return role != null ? role.toString() : null;
    }
}
