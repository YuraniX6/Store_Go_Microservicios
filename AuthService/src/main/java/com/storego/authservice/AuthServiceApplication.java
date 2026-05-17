package com.storego.authservice;

import com.storego.authservice.entity.Role;
import com.storego.authservice.entity.User;
import com.storego.authservice.repository.RoleRepository;
import com.storego.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@Slf4j
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("Initializing application data...");

            // Create ADMIN role if not exists
            if (roleRepository.findByName("ADMIN").isEmpty()) {
                Role adminRole = Role.builder()
                        .name("ADMIN")
                        .build();
                roleRepository.save(adminRole);
                log.info("ADMIN role created");
            }

            // Create USER role if not exists
            if (roleRepository.findByName("USER").isEmpty()) {
                Role userRole = Role.builder()
                        .name("USER")
                        .build();
                roleRepository.save(userRole);
                log.info("USER role created");
            }

            // Create admin user if not exists
            if (!userRepository.existsByUsername("admin")) {
                Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
                User adminUser = User.builder()
                        .username("admin")
                        .email("admin@storego.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(adminRole)
                        .build();
                userRepository.save(adminUser);
                log.info("Admin user created");
            }

            log.info("Application data initialization completed");
        };
    }
}
