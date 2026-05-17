package com.storego.authservice.service;

import com.storego.authservice.entity.Role;
import com.storego.authservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Base64;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=YXV0aEtzZWNyZXRrZXlmb3JzdG9yZWdvbWljcm9zZXJ2aWNlYXV0aGVudGljYXRpb24=",
        "jwt.expiration=3600000"
})
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .id(1L)
                .name("USER")
                .build();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .role(testRole)
                .build();
    }

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(testUser.getId().toString(), jwtService.extractSubject(token));
    }

    @Test
    void testGenerateRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken(testUser);

        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertEquals(testUser.getId().toString(), jwtService.extractSubject(refreshToken));
    }

    @Test
    void testExtractSubject() {
        String token = jwtService.generateToken(testUser);
        String subject = jwtService.extractSubject(token);

        assertEquals(testUser.getId().toString(), subject);
    }

    @Test
    void testExtractUsername() {
        String token = jwtService.generateToken(testUser);
        String username = jwtService.extractUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    void testExtractRoles() {
        String token = jwtService.generateToken(testUser);
        Set<Long> roles = jwtService.extractRoles(token);

        assertNotNull(roles);
        assertTrue(roles.contains(1L));
    }

    @Test
    void testIsTokenValid_Valid() {
        String token = jwtService.generateToken(testUser);
        boolean isValid = jwtService.isTokenValid(token, testUser.getId().toString());

        assertTrue(isValid);
    }

    @Test
    void testIsTokenValid_InvalidUserId() {
        String token = jwtService.generateToken(testUser);
        String differentUserId = UUID.randomUUID().toString();
        boolean isValid = jwtService.isTokenValid(token, differentUserId);

        assertFalse(isValid);
    }

    @Test
    void testIsTokenValid_InvalidToken() {
        String invalidToken = "invalid.token.here";
        boolean isValid = jwtService.isTokenValid(invalidToken, testUser.getId().toString());

        assertFalse(isValid);
    }

    @Test
    void testTokenExpiration() {
        String token = jwtService.generateToken(testUser);
        assertFalse(jwtService.isTokenValid(token, testUser.getId().toString()));
    }
}
