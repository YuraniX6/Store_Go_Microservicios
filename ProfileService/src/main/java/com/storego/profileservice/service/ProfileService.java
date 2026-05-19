package com.storego.profileservice.service;

import com.storego.profileservice.dto.CreateProfileRequest;
import com.storego.profileservice.dto.UpdateProfileRequest;
import com.storego.profileservice.entity.Profile;
import com.storego.profileservice.exception.ProfileAlreadyExistsException;
import com.storego.profileservice.exception.ProfileNotFoundException;
import com.storego.profileservice.exception.RutAlreadyTakenException;
import com.storego.profileservice.repository.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public Profile create(UUID userId, CreateProfileRequest request) {
        log.info("Creando perfil para usuario: {}", userId);

        if (profileRepository.existsById(userId)) {
            log.warn("Intento de crear perfil duplicado para usuario: {}", userId);
            throw new ProfileAlreadyExistsException(
                    "Profile already exists for user " + userId
            );
        }

        if (profileRepository.existsByRut(request.getRut())) {
            log.warn("RUT duplicado intentado: {}", request.getRut());
            throw new RutAlreadyTakenException(
                    "RUT " + request.getRut() + " already exists"
            );
        }

        try {
            Profile profile = Profile.builder()
                    .userId(userId)
                    .fullname(request.getFullname())
                    .rut(request.getRut())
                    .language(request.getLanguage())
                    .description(request.getDescription())
                    .build();

            Profile saved = profileRepository.save(profile);
            log.info("Perfil creado exitosamente para usuario: {}", userId);
            return saved;
        } catch (DataIntegrityViolationException ex) {
            log.error("Violación de integridad al crear perfil: {}", ex.getMessage());
            String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
            if (msg.contains("uk_profile_rut") || msg.contains("rut")) {
                throw new RutAlreadyTakenException("RUT " + request.getRut() + " already exists");
            }
            throw new ProfileAlreadyExistsException("Profile already exists for user " + userId);
        }
    }

    @Transactional(readOnly = true)
    public Profile getMyProfile(UUID userId) {
        log.info("Obteniendo perfil propio del usuario: {}", userId);
        return profileRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Perfil no encontrado para usuario: {}", userId);
                    return new ProfileNotFoundException(
                            "Profile not found for user " + userId
                    );
                });
    }

    @Transactional(readOnly = true)
    public Profile getPublicProfile(UUID targetUuid) {
        log.info("Obteniendo perfil público del usuario: {}", targetUuid);
        return profileRepository.findById(targetUuid)
                .orElseThrow(() -> {
                    log.warn("Perfil público no encontrado para usuario: {}", targetUuid);
                    return new ProfileNotFoundException(
                            "Profile not found for user " + targetUuid
                    );
                });
    }

    public Profile update(UUID userId, UpdateProfileRequest request) {
        log.info("Actualizando perfil del usuario: {}", userId);

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Intento de actualizar perfil inexistente para usuario: {}", userId);
                    return new ProfileNotFoundException(
                            "Profile not found for user " + userId
                    );
                });

        if (request.getFullname() != null && !request.getFullname().isBlank()) {
            profile.setFullname(request.getFullname());
            log.debug("Fullname actualizado a: {}", request.getFullname());
        }

        if (request.getLanguage() != null && !request.getLanguage().isBlank()) {
            profile.setLanguage(request.getLanguage());
            log.debug("Language actualizado a: {}", request.getLanguage());
        }

        if (request.getDescription() != null) {
            profile.setDescription(request.getDescription());
            log.debug("Description actualizado");
        }

        Profile updated = profileRepository.save(profile);
        log.info("Perfil actualizado exitosamente para usuario: {}", userId);
        return updated;
    }
}
