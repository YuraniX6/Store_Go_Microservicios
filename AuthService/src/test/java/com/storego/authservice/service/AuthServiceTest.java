package com.storego.authservice.service;

import com.storego.authservice.dto.AuthResponse;
import com.storego.authservice.dto.LoginRequest;
import com.storego.authservice.dto.RefreshRequest;
import com.storego.authservice.dto.RegisterRequest;
import com.storego.authservice.entity.Role;
import com.storego.authservice.entity.User;
import com.storego.authservice.repository.RoleRepository;
import com.storego.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private Role userRole;
    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        userRole = Role.builder()
                .id(1L)
                .name("USER")
                .build();

        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .role(userRole)
                .build();
    }

    @Test
    void testRegister_Success() {
        RegisterRequest registerRequest = new RegisterRequest(
                "newuser",
                "newuser@example.com",
                "password123"
        );

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("jwt-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_UsernameTaken() {
        RegisterRequest registerRequest = new RegisterRequest(
                "existinguser",
                "new@example.com",
                "password123"
        );

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.register(registerRequest)
        );

        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void testRegister_EmailTaken() {
        RegisterRequest registerRequest = new RegisterRequest(
                "newuser",
                "existing@example.com",
                "password123"
        );

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.register(registerRequest)
        );

        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void testLogin_Success() {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser", null));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("jwt-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testRefreshToken_Success() {
        RefreshRequest refreshRequest = new RefreshRequest("refresh-token");

        when(jwtService.extractSubject("refresh-token")).thenReturn(testUserId.toString());
        when(jwtService.isTokenValid("refresh-token", testUserId.toString())).thenReturn(true);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(User.class))).thenReturn("new-jwt-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("new-refresh-token");

        AuthResponse response = authService.refreshToken(refreshRequest);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("new-jwt-token", response.getToken());
    }

    @Test
    void testRefreshToken_InvalidToken() {
        RefreshRequest refreshRequest = new RefreshRequest("invalid-token");

        when(jwtService.extractSubject("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.refreshToken(refreshRequest)
        );

        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void testValidateToken_Valid() {
        when(jwtService.extractSubject("valid-token")).thenReturn(testUserId.toString());
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid("valid-token", testUserId.toString())).thenReturn(true);

        boolean isValid = authService.validateToken("valid-token");

        assertTrue(isValid);
    }

    @Test
    void testValidateToken_Invalid() {
        when(jwtService.extractSubject("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        boolean isValid = authService.validateToken("invalid-token");

        assertFalse(isValid);
    }
}
