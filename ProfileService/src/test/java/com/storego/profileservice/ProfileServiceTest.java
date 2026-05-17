package com.storego.profileservice;

import com.storego.profileservice.dto.CreateProfileRequest;
import com.storego.profileservice.dto.UpdateProfileRequest;
import com.storego.profileservice.entity.Profile;
import com.storego.profileservice.exception.ProfileAlreadyExistsException;
import com.storego.profileservice.exception.ProfileNotFoundException;
import com.storego.profileservice.exception.RutAlreadyTakenException;
import com.storego.profileservice.repository.ProfileRepository;
import com.storego.profileservice.service.ProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileService profileService;

    private UUID testUserId;
    private CreateProfileRequest createRequest;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        createRequest = CreateProfileRequest.builder()
                .fullname("Diego Castillo")
                .rut("12345678-9")
                .language("es")
                .description("Coleccionista de Dragon Lore")
                .build();

        testProfile = Profile.builder()
                .userId(testUserId)
                .fullname("Diego Castillo")
                .rut("12345678-9")
                .language("es")
                .description("Coleccionista de Dragon Lore")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void testCreateProfileSuccess() {
        when(profileRepository.existsById(testUserId)).thenReturn(false);
        when(profileRepository.existsByRut(createRequest.getRut())).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        Profile result = profileService.create(testUserId, createRequest);

        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals("Diego Castillo", result.getFullname());
        assertEquals("12345678-9", result.getRut());
        assertEquals("es", result.getLanguage());

        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    void testCreateProfileAlreadyExists() {
        when(profileRepository.existsById(testUserId)).thenReturn(true);

        ProfileAlreadyExistsException exception = assertThrows(
                ProfileAlreadyExistsException.class,
                () -> profileService.create(testUserId, createRequest)
        );

        assertEquals("Profile already exists for user " + testUserId, exception.getMessage());
        verify(profileRepository, never()).save(any());
    }

    @Test
    void testCreateProfileRutDuplicate() {
        when(profileRepository.existsById(testUserId)).thenReturn(false);
        when(profileRepository.existsByRut(createRequest.getRut())).thenReturn(true);

        RutAlreadyTakenException exception = assertThrows(
                RutAlreadyTakenException.class,
                () -> profileService.create(testUserId, createRequest)
        );

        assertEquals("RUT 12345678-9 already exists", exception.getMessage());
        verify(profileRepository, never()).save(any());
    }

    @Test
    void testGetMyProfileSuccess() {
        when(profileRepository.findById(testUserId)).thenReturn(Optional.of(testProfile));

        Profile result = profileService.getMyProfile(testUserId);

        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals("Diego Castillo", result.getFullname());

        verify(profileRepository, times(1)).findById(testUserId);
    }

    @Test
    void testGetMyProfileNotFound() {
        when(profileRepository.findById(testUserId)).thenReturn(Optional.empty());

        ProfileNotFoundException exception = assertThrows(
                ProfileNotFoundException.class,
                () -> profileService.getMyProfile(testUserId)
        );

        assertEquals("Profile not found for user " + testUserId, exception.getMessage());
    }

    @Test
    void testUpdateProfileSuccess() {
        UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
                .fullname("Diego Updated")
                .language("en")
                .description("Updated description")
                .build();

        Profile existingProfile = Profile.builder()
                .userId(testUserId)
                .fullname("Diego Castillo")
                .rut("12345678-9")
                .language("es")
                .description("Original description")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(profileRepository.findById(testUserId)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(existingProfile);

        Profile result = profileService.update(testUserId, updateRequest);

        assertNotNull(result);
        assertEquals("Diego Updated", result.getFullname());
        assertEquals("en", result.getLanguage());
        assertEquals("Updated description", result.getDescription());

        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    void testUpdateProfileNotFound() {
        UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
                .fullname("Diego Updated")
                .build();

        when(profileRepository.findById(testUserId)).thenReturn(Optional.empty());

        ProfileNotFoundException exception = assertThrows(
                ProfileNotFoundException.class,
                () -> profileService.update(testUserId, updateRequest)
        );

        assertEquals("Profile not found for user " + testUserId, exception.getMessage());
        verify(profileRepository, never()).save(any());
    }

    @Test
    void testGetPublicProfileSuccess() {
        when(profileRepository.findById(testUserId)).thenReturn(Optional.of(testProfile));

        Profile result = profileService.getPublicProfile(testUserId);

        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals("Diego Castillo", result.getFullname());

        verify(profileRepository, times(1)).findById(testUserId);
    }

    @Test
    void testGetPublicProfileNotFound() {
        when(profileRepository.findById(testUserId)).thenReturn(Optional.empty());

        ProfileNotFoundException exception = assertThrows(
                ProfileNotFoundException.class,
                () -> profileService.getPublicProfile(testUserId)
        );

        assertEquals("Profile not found for user " + testUserId, exception.getMessage());
    }
}
