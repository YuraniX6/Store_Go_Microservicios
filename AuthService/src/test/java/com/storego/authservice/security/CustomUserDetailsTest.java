package com.storego.authservice.security;

import com.storego.authservice.entity.Role;
import com.storego.authservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    private CustomUserDetails customUserDetails;
    private User testUser;

    @BeforeEach
    void setUp() {
        Role userRole = Role.builder()
                .id(1L)
                .name("USER")
                .build();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .role(userRole)
                .build();

        customUserDetails = new CustomUserDetails(testUser);
    }

    @Test
    void testGetAuthorities_WithRole() {
        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testGetAuthorities_WithoutRole() {
        testUser.setRole(null);
        CustomUserDetails customUserDetailsNoRole = new CustomUserDetails(testUser);

        Collection<? extends GrantedAuthority> authorities = customUserDetailsNoRole.getAuthorities();

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testGetAuthorities_AdminRole() {
        Role adminRole = Role.builder()
                .id(2L)
                .name("ADMIN")
                .build();
        testUser.setRole(adminRole);
        CustomUserDetails customUserDetailsAdmin = new CustomUserDetails(testUser);

        Collection<? extends GrantedAuthority> authorities = customUserDetailsAdmin.getAuthorities();

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testGetPassword() {
        assertEquals("hashedPassword", customUserDetails.getPassword());
    }

    @Test
    void testGetUsername() {
        assertEquals("testuser", customUserDetails.getUsername());
    }

    @Test
    void testGetUserId() {
        assertEquals(testUser.getId().toString(), customUserDetails.getUserId());
    }

    @Test
    void testGetUser() {
        assertEquals(testUser, customUserDetails.getUser());
    }

    @Test
    void testIsAccountNonExpired() {
        assertTrue(customUserDetails.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked() {
        assertTrue(customUserDetails.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired() {
        assertTrue(customUserDetails.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled() {
        assertTrue(customUserDetails.isEnabled());
    }
}
