package com.storego.authservice.service;

import com.storego.authservice.entity.Role;
import com.storego.authservice.entity.User;
import com.storego.authservice.repository.UserRepository;
import com.storego.authservice.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        Role userRole = Role.builder()
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
    void testLoadUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals(testUserId.toString(), userDetails.getUserId());
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("nonexistent")
        );

        assertTrue(exception.getMessage().contains("nonexistent"));
    }

    @Test
    void testLoadUserById_Success() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserById(testUserId);

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals(testUserId.toString(), userDetails.getUserId());
    }

    @Test
    void testLoadUserById_NotFound() {
        UUID nonexistentId = UUID.randomUUID();
        when(userRepository.findById(nonexistentId)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserById(nonexistentId)
        );

        assertTrue(exception.getMessage().contains(nonexistentId.toString()));
    }

    @Test
    void testLoadUserByUsername_WithRole() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}
