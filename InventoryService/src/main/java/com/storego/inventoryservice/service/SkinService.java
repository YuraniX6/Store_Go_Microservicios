package com.storego.inventoryservice.service;

import com.storego.inventoryservice.dto.CreateSkinRequest;
import com.storego.inventoryservice.dto.SkinResponse;
import com.storego.inventoryservice.dto.UpdateSkinRequest;
import com.storego.inventoryservice.entity.Skin;
import com.storego.inventoryservice.exception.SkinAccessDeniedException;
import com.storego.inventoryservice.exception.SkinNotFoundException;
import com.storego.inventoryservice.repository.SkinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SkinService {

    private final SkinRepository skinRepository;

    private SkinResponse mapToResponse(Skin skin) {
        return SkinResponse.builder()
                .id(skin.getId())
                .ownerId(skin.getOwnerId())
                .name(skin.getName())
                .weapon(skin.getWeapon())
                .rarity(skin.getRarity())
                .wear(skin.getWear())
                .floatValue(skin.getFloatValue())
                .imageUrl(skin.getImageUrl())
                .createdAt(skin.getCreatedAt())
                .updatedAt(skin.getUpdatedAt())
                .build();
    }

    public SkinResponse create(UUID ownerId, CreateSkinRequest request) {
        log.info("Creating new skin for owner: {}", ownerId);

        Skin skin = Skin.builder()
                .ownerId(ownerId)
                .name(request.getName())
                .weapon(request.getWeapon())
                .rarity(request.getRarity())
                .wear(request.getWear())
                .floatValue(request.getFloatValue())
                .imageUrl(request.getImageUrl())
                .build();

        Skin savedSkin = skinRepository.save(skin);
        log.info("Skin created with id: {} for owner: {}", savedSkin.getId(), ownerId);

        return mapToResponse(savedSkin);
    }

    @Transactional(readOnly = true)
    public List<SkinResponse> getMySkins(UUID ownerId) {
        log.info("Fetching all skins for owner: {}", ownerId);

        List<Skin> skins = skinRepository.findAllByOwnerId(ownerId);
        log.info("Found {} skins for owner: {}", skins.size(), ownerId);

        return skins.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SkinResponse getSkin(UUID ownerId, UUID skinId) {
        log.info("Fetching skin: {} for owner: {}", skinId, ownerId);

        Skin skin = skinRepository.findById(skinId)
                .orElseThrow(() -> {
                    log.warn("Skin not found: {}", skinId);
                    return new SkinNotFoundException("Skin not found: " + skinId);
                });

        assertOwnership(skin, ownerId);
        return mapToResponse(skin);
    }

    public SkinResponse update(UUID ownerId, UUID skinId, UpdateSkinRequest request) {
        log.info("Updating skin: {} for owner: {}", skinId, ownerId);

        Skin skin = skinRepository.findById(skinId)
                .orElseThrow(() -> {
                    log.warn("Skin not found: {}", skinId);
                    return new SkinNotFoundException("Skin not found: " + skinId);
                });

        assertOwnership(skin, ownerId);

        skin.setName(request.getName());
        skin.setWeapon(request.getWeapon());
        skin.setRarity(request.getRarity());
        skin.setWear(request.getWear());
        skin.setFloatValue(request.getFloatValue());
        skin.setImageUrl(request.getImageUrl());

        Skin updatedSkin = skinRepository.save(skin);
        log.info("Skin updated: {} for owner: {}", skinId, ownerId);

        return mapToResponse(updatedSkin);
    }

    public void delete(UUID ownerId, UUID skinId) {
        log.info("Deleting skin: {} for owner: {}", skinId, ownerId);

        Skin skin = skinRepository.findById(skinId)
                .orElseThrow(() -> {
                    log.warn("Skin not found: {}", skinId);
                    return new SkinNotFoundException("Skin not found: " + skinId);
                });

        assertOwnership(skin, ownerId);
        skinRepository.deleteById(skinId);
        log.info("Skin deleted: {} for owner: {}", skinId, ownerId);
    }

    private void assertOwnership(Skin skin, UUID ownerId) {
        if (!skin.getOwnerId().equals(ownerId)) {
            log.warn("Access denied: User {} attempted to access skin {} owned by {}", ownerId, skin.getId(),
                    skin.getOwnerId());
            throw new SkinAccessDeniedException("You do not have permission to access this skin");
        }
    }
}
