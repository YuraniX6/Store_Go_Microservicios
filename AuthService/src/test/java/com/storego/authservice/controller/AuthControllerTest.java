package com.storego.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storego.authservice.dto.*;
import com.storego.authservice.entity.Role;
import com.storego.authservice.entity.User;
import com.storego.authservice.security.CustomUserDetails;
import com.storego.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private UUID testUserId;
    private Role userRole;
    private User testUser;

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
    void testRegister_Success() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "newuser",
                "newuser@example.com",
                "password123"
        );

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token")
                .refreshToken("refresh-token")
                .username("newuser")
                .roles(Set.of(1L))
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token")
                .refreshToken("refresh-token")
                .username("testuser")
                .roles(Set.of(1L))
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void testValidateToken_Valid() throws Exception {
        when(authService.validateToken("valid-token")).thenReturn(true);

        mockMvc.perform(get("/auth/validate")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testValidateToken_Invalid() throws Exception {
        when(authService.validateToken("invalid-token")).thenReturn(false);

        mockMvc.perform(get("/auth/validate")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testValidateToken_NoToken() throws Exception {
        mockMvc.perform(get("/auth/validate"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testRegister_InvalidUsername() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "ab", // Too short
                "valid@example.com",
                "password123"
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_InvalidEmail() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "validuser",
                "invalid-email", // Invalid email
                "password123"
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_ShortPassword() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "validuser",
                "valid@example.com",
                "pass" // Too short
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
}
