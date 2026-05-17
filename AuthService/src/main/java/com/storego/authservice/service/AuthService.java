package com.storego.authservice.service;

import com.storego.authservice.dto.AuthResponse;
import com.storego.authservice.dto.LoginRequest;
import com.storego.authservice.dto.RefreshRequest;
import com.storego.authservice.dto.RegisterRequest;
import com.storego.authservice.entity.Role;
import com.storego.authservice.entity.User;
import com.storego.authservice.repository.RoleRepository;
import com.storego.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        log.info("Registering user: {}", registerRequest.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Get or create USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name("USER")
                            .build();
                    return roleRepository.save(newRole);
                });

        // Create new user
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(userRole)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getId());

        return generateAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Get user
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Assign USER role if no role assigned
        if (user.getRole() == null) {
            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> {
                        Role newRole = Role.builder()
                                .name("USER")
                                .build();
                        return roleRepository.save(newRole);
                    });
            user.setRole(userRole);
            user = userRepository.save(user);
        }

        log.info("User logged in successfully: {}", user.getId());
        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshRequest refreshRequest) {
        log.info("Refreshing token");

        String refreshToken = refreshRequest.getRefreshToken();

        // Extract userId from refresh token
        String userId;
        try {
            userId = jwtService.extractSubject(refreshToken);
        } catch (Exception e) {
            log.error("Invalid refresh token");
            throw new RuntimeException("Invalid refresh token");
        }

        // Validate refresh token
        if (!jwtService.isTokenValid(refreshToken, userId)) {
            throw new RuntimeException("Refresh token has expired or is invalid");
        }

        // Get user
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Token refreshed successfully for user: {}", userId);
        return generateAuthResponse(user);
    }

    public boolean validateToken(String token) {
        try {
            String userId = jwtService.extractSubject(token);
            Optional<User> user = userRepository.findById(UUID.fromString(userId));
            if (user.isEmpty()) {
                return false;
            }
            return jwtService.isTokenValid(token, userId);
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    private AuthResponse generateAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        Set<Long> roleIds = user.getRole() != null 
            ? Set.of(user.getRole().getId()) 
            : Set.of();

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .roles(roleIds)
                .build();
    }
}
