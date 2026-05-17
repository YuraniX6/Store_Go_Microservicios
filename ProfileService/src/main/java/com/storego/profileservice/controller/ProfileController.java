package com.storego.profileservice.controller;

import com.storego.profileservice.dto.CreateProfileRequest;
import com.storego.profileservice.dto.ProfileResponse;
import com.storego.profileservice.dto.PublicProfileResponse;
import com.storego.profileservice.dto.UpdateProfileRequest;
import com.storego.profileservice.entity.Profile;
import com.storego.profileservice.mapper.ProfileMapper;
import com.storego.profileservice.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Profiles", description = "User profile management endpoints")
@Slf4j
public class ProfileController {

    private final ProfileService profileService;
    private final ProfileMapper profileMapper;

    public ProfileController(ProfileService profileService, ProfileMapper profileMapper) {
        this.profileService = profileService;
        this.profileMapper = profileMapper;
    }

    @PostMapping
    @Operation(
            summary = "Create user profile",
            description = "Creates a new profile for the authenticated user. Returns 409 if profile already exists or RUT is already taken.",
            security = @SecurityRequirement(name = "Bearer JWT")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Perfil creado exitosamente", 
                    content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (validación fallida)"),
            @ApiResponse(responseCode = "409", description = "El perfil ya existe o el RUT está en uso"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<ProfileResponse> createProfile(
            @Valid @RequestBody CreateProfileRequest request,
            Authentication authentication) {

        UUID userId = extractUserIdFromAuthentication(authentication);
        log.info("POST /users - Creating profile for user: {}", userId);

        Profile profile = profileService.create(userId, request);
        ProfileResponse response = profileMapper.toProfileResponse(profile);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get my profile",
            description = "Retrieves the authenticated user's full profile with RUT.",
            security = @SecurityRequirement(name = "Bearer JWT")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<ProfileResponse> getMyProfile(Authentication authentication) {
        UUID userId = extractUserIdFromAuthentication(authentication);
        log.info("GET /users/me - Getting profile for user: {}", userId);

        Profile profile = profileService.getMyProfile(userId);
        ProfileResponse response = profileMapper.toProfileResponse(profile);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uuid}")
    @Operation(
            summary = "Get public profile",
            description = "Retrieves a public profile by UUID. Returns profile without RUT.",
            security = @SecurityRequirement(name = "Bearer JWT")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil público obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = PublicProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<PublicProfileResponse> getPublicProfile(
            @PathVariable UUID uuid) {

        log.info("GET /users/{} - Getting public profile", uuid);

        Profile profile = profileService.getPublicProfile(uuid);
        PublicProfileResponse response = profileMapper.toPublicProfileResponse(profile);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    @Operation(
            summary = "Update my profile",
            description = "Updates the authenticated user's profile. RUT cannot be modified. All fields are optional.",
            security = @SecurityRequirement(name = "Bearer JWT")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (validación fallida)"),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<ProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {

        UUID userId = extractUserIdFromAuthentication(authentication);
        log.info("PATCH /users/me - Updating profile for user: {}", userId);

        Profile profile = profileService.update(userId, request);
        ProfileResponse response = profileMapper.toProfileResponse(profile);

        return ResponseEntity.ok(response);
    }

    private UUID extractUserIdFromAuthentication(Authentication authentication) {
        try {
            String principal = (String) authentication.getPrincipal();
            return UUID.fromString(principal);
        } catch (ClassCastException | IllegalArgumentException ex) {
            log.error("Error al extraer UUID del Authentication: {}", ex.getMessage());
            throw new IllegalArgumentException("Invalid user ID format");
        }
    }
}
