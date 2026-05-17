package com.storego.profileservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storego.profileservice.controller.ProfileController;
import com.storego.profileservice.dto.CreateProfileRequest;
import com.storego.profileservice.dto.UpdateProfileRequest;
import com.storego.profileservice.entity.Profile;
import com.storego.profileservice.exception.ProfileAlreadyExistsException;
import com.storego.profileservice.exception.ProfileNotFoundException;
import com.storego.profileservice.mapper.ProfileMapper;
import com.storego.profileservice.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private ProfileMapper profileMapper;

    private UUID testUserId;
    private Profile testProfile;
    private CreateProfileRequest createRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testProfile = Profile.builder()
                .userId(testUserId)
                .fullname("Diego Castillo")
                .rut("12345678-9")
                .language("es")
                .description("Coleccionista de Dragon Lore")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        createRequest = CreateProfileRequest.builder()
                .fullname("Diego Castillo")
                .rut("12345678-9")
                .language("es")
                .description("Coleccionista de Dragon Lore")
                .build();
    }

    @Test
    void testCreateProfileSuccess() throws Exception {
        when(profileService.create(eq(testUserId), any(CreateProfileRequest.class)))
                .thenReturn(testProfile);
        when(profileMapper.toProfileResponse(testProfile))
                .thenReturn(com.storego.profileservice.dto.ProfileResponse.builder()
                        .userId(testUserId)
                        .fullname("Diego Castillo")
                        .rut("12345678-9")
                        .language("es")
                        .description("Coleccionista de Dragon Lore")
                        .createdAt(testProfile.getCreatedAt())
                        .updatedAt(testProfile.getUpdatedAt())
                        .build());

        mockMvc.perform(post("/users")
                .with(jwt().jwt(builder -> builder.subject(testUserId.toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.fullname", containsString("Diego")))
                .andExpect(jsonPath("$.rut", containsString("12345678")));
    }

    @Test
    void testCreateProfileAlreadyExists() throws Exception {
        when(profileService.create(eq(testUserId), any(CreateProfileRequest.class)))
                .thenThrow(new ProfileAlreadyExistsException("Profile already exists"));

        mockMvc.perform(post("/users")
                .with(jwt().jwt(builder -> builder.subject(testUserId.toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error", containsString("Conflict")));
    }

    @Test
    void testGetMyProfileSuccess() throws Exception {
        when(profileService.getMyProfile(eq(testUserId)))
                .thenReturn(testProfile);
        when(profileMapper.toProfileResponse(testProfile))
                .thenReturn(com.storego.profileservice.dto.ProfileResponse.builder()
                        .userId(testUserId)
                        .fullname("Diego Castillo")
                        .rut("12345678-9")
                        .language("es")
                        .description("Coleccionista de Dragon Lore")
                        .createdAt(testProfile.getCreatedAt())
                        .updatedAt(testProfile.getUpdatedAt())
                        .build());

        mockMvc.perform(get("/users/me")
                .with(jwt().jwt(builder -> builder.subject(testUserId.toString())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.rut", containsString("12345678")));
    }

    @Test
    void testGetMyProfileNotFound() throws Exception {
        when(profileService.getMyProfile(eq(testUserId)))
                .thenThrow(new ProfileNotFoundException("Profile not found"));

        mockMvc.perform(get("/users/me")
                .with(jwt().jwt(builder -> builder.subject(testUserId.toString())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error", containsString("Not Found")));
    }

    @Test
    void testGetPublicProfileSuccess() throws Exception {
        when(profileService.getPublicProfile(eq(testUserId)))
                .thenReturn(testProfile);
        when(profileMapper.toPublicProfileResponse(testProfile))
                .thenReturn(com.storego.profileservice.dto.PublicProfileResponse.builder()
                        .userId(testUserId)
                        .fullname("Diego Castillo")
                        .language("es")
                        .description("Coleccionista de Dragon Lore")
                        .createdAt(testProfile.getCreatedAt())
                        .build());

        mockMvc.perform(get("/users/" + testUserId)
                .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.fullname", containsString("Diego")))
                .andExpect(jsonPath("$.rut").doesNotExist());
    }

    @Test
    void testGetPublicProfileNotFound() throws Exception {
        when(profileService.getPublicProfile(eq(testUserId)))
                .thenThrow(new ProfileNotFoundException("Profile not found"));

        mockMvc.perform(get("/users/" + testUserId)
                .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testUpdateProfileSuccess() throws Exception {
        UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
                .fullname("Diego Updated")
                .language("en")
                .build();

        Profile updatedProfile = Profile.builder()
                .userId(testUserId)
                .fullname("Diego Updated")
                .rut("12345678-9")
                .language("en")
                .description("Coleccionista de Dragon Lore")
                .createdAt(testProfile.getCreatedAt())
                .updatedAt(Instant.now())
                .build();

        when(profileService.update(eq(testUserId), any(UpdateProfileRequest.class)))
                .thenReturn(updatedProfile);
        when(profileMapper.toProfileResponse(updatedProfile))
                .thenReturn(com.storego.profileservice.dto.ProfileResponse.builder()
                        .userId(testUserId)
                        .fullname("Diego Updated")
                        .rut("12345678-9")
                        .language("en")
                        .description("Coleccionista de Dragon Lore")
                        .createdAt(updatedProfile.getCreatedAt())
                        .updatedAt(updatedProfile.getUpdatedAt())
                        .build());

        mockMvc.perform(patch("/users/me")
                .with(jwt().jwt(builder -> builder.subject(testUserId.toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullname", containsString("Updated")))
                .andExpect(jsonPath("$.language").value("en"));
    }

    @Test
    void testUpdateProfileNotFound() throws Exception {
        UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
                .fullname("Diego Updated")
                .build();

        when(profileService.update(eq(testUserId), any(UpdateProfileRequest.class)))
                .thenThrow(new ProfileNotFoundException("Profile not found"));

        mockMvc.perform(patch("/users/me")
                .with(jwt().jwt(builder -> builder.subject(testUserId.toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testCreateProfileValidationFail() throws Exception {
        CreateProfileRequest invalidRequest = CreateProfileRequest.builder()
                .fullname("")
                .rut("12345678-9")
                .language("xyz")
                .build();

        mockMvc.perform(post("/users")
                .with(jwt().jwt(builder -> builder.subject(testUserId.toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testNoAuthenticationRequired() throws Exception {
        mockMvc.perform(get("/users/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
