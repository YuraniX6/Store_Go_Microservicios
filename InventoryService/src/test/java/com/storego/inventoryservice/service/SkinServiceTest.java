package com.storego.inventoryservice.service;

import com.storego.inventoryservice.dto.CreateSkinRequest;
import com.storego.inventoryservice.dto.SkinResponse;
import com.storego.inventoryservice.dto.UpdateSkinRequest;
import com.storego.inventoryservice.entity.Rarity;
import com.storego.inventoryservice.entity.Skin;
import com.storego.inventoryservice.entity.Wear;
import com.storego.inventoryservice.exception.SkinAccessDeniedException;
import com.storego.inventoryservice.exception.SkinNotFoundException;
import com.storego.inventoryservice.repository.SkinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkinServiceTest {

    @Mock
    private SkinRepository skinRepository;

    @InjectMocks
    private SkinService skinService;

    private UUID ownerId;
    private UUID skinId;
    private Skin testSkin;
    private SkinResponse testSkinResponse;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        skinId = UUID.randomUUID();

        testSkin = Skin.builder()
                .id(skinId)
                .ownerId(ownerId)
                .name("AWP | Dragon Lore")
                .weapon("AWP")
                .rarity(Rarity.COVERT)
                .wear(Wear.FACTORY_NEW)
                .floatValue(new BigDecimal("0.012345"))
                .imageUrl("https://example.com/skin.png")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        testSkinResponse = SkinResponse.builder()
                .id(skinId)
                .ownerId(ownerId)
                .name("AWP | Dragon Lore")
                .weapon("AWP")
                .rarity(Rarity.COVERT)
                .wear(Wear.FACTORY_NEW)
                .floatValue(new BigDecimal("0.012345"))
                .imageUrl("https://example.com/skin.png")
                .createdAt(testSkin.getCreatedAt())
                .updatedAt(testSkin.getUpdatedAt())
                .build();
    }

    @Test
    void testCreateSkin_Success() {
        CreateSkinRequest request = CreateSkinRequest.builder()
                .name("AWP | Dragon Lore")
                .weapon("AWP")
                .rarity(Rarity.COVERT)
                .wear(Wear.FACTORY_NEW)
                .floatValue(new BigDecimal("0.012345"))
                .imageUrl("https://example.com/skin.png")
                .build();

        when(skinRepository.save(any(Skin.class))).thenReturn(testSkin);

        SkinResponse response = skinService.create(ownerId, request);

        assertNotNull(response);
        assertEquals(skinId, response.getId());
        assertEquals("AWP | Dragon Lore", response.getName());

        verify(skinRepository, times(1)).save(any(Skin.class));
    }

    @Test
    void testGetMySkins_Success() {
        UUID skinId2 = UUID.randomUUID();
        Skin testSkin2 = Skin.builder()
                .id(skinId2)
                .ownerId(ownerId)
                .name("AK-47 | Redline")
                .weapon("AK-47")
                .rarity(Rarity.CLASSIFIED)
                .wear(Wear.MINIMAL_WEAR)
                .floatValue(new BigDecimal("0.25"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        SkinResponse response2 = SkinResponse.builder()
                .id(skinId2)
                .ownerId(ownerId)
                .name("AK-47 | Redline")
                .weapon("AK-47")
                .rarity(Rarity.CLASSIFIED)
                .wear(Wear.MINIMAL_WEAR)
                .floatValue(new BigDecimal("0.25"))
                .build();

        when(skinRepository.findAllByOwnerId(ownerId)).thenReturn(Arrays.asList(testSkin, testSkin2));

        List<SkinResponse> responses = skinService.getMySkins(ownerId);

        assertEquals(2, responses.size());
        assertEquals("AWP | Dragon Lore", responses.get(0).getName());
        assertEquals("AK-47 | Redline", responses.get(1).getName());

        verify(skinRepository, times(1)).findAllByOwnerId(ownerId);
    }

    @Test
    void testGetMySkins_Empty() {
        when(skinRepository.findAllByOwnerId(ownerId)).thenReturn(Arrays.asList());

        List<SkinResponse> responses = skinService.getMySkins(ownerId);

        assertEquals(0, responses.size());
        verify(skinRepository, times(1)).findAllByOwnerId(ownerId);
    }

    @Test
    void testGetSkin_Success() {
        when(skinRepository.findById(skinId)).thenReturn(Optional.of(testSkin));

        SkinResponse response = skinService.getSkin(ownerId, skinId);

        assertNotNull(response);
        assertEquals(skinId, response.getId());

        verify(skinRepository, times(1)).findById(skinId);
    }

    @Test
    void testGetSkin_NotFound() {
        when(skinRepository.findById(skinId)).thenReturn(Optional.empty());

        assertThrows(SkinNotFoundException.class, () -> skinService.getSkin(ownerId, skinId));

        verify(skinRepository, times(1)).findById(skinId);
    }

    @Test
    void testGetSkin_AccessDenied() {
        UUID anotherUserId = UUID.randomUUID();
        when(skinRepository.findById(skinId)).thenReturn(Optional.of(testSkin));

        assertThrows(SkinAccessDeniedException.class, () -> skinService.getSkin(anotherUserId, skinId));

        verify(skinRepository, times(1)).findById(skinId);
    }

    @Test
    void testUpdateSkin_Success() {
        UpdateSkinRequest request = UpdateSkinRequest.builder()
                .name("AWP | Dragon Lore (Updated)")
                .weapon("AWP")
                .rarity(Rarity.COVERT)
                .wear(Wear.MINIMAL_WEAR)
                .floatValue(new BigDecimal("0.02"))
                .imageUrl("https://example.com/updated.png")
                .build();

        Skin updatedSkin = Skin.builder()
                .id(skinId)
                .ownerId(ownerId)
                .name("AWP | Dragon Lore (Updated)")
                .weapon("AWP")
                .rarity(Rarity.COVERT)
                .wear(Wear.MINIMAL_WEAR)
                .floatValue(new BigDecimal("0.02"))
                .imageUrl("https://example.com/updated.png")
                .createdAt(testSkin.getCreatedAt())
                .updatedAt(Instant.now())
                .build();

        SkinResponse updatedResponse = SkinResponse.builder()
                .id(skinId)
                .ownerId(ownerId)
                .name("AWP | Dragon Lore (Updated)")
                .weapon("AWP")
                .rarity(Rarity.COVERT)
                .wear(Wear.MINIMAL_WEAR)
                .floatValue(new BigDecimal("0.02"))
                .imageUrl("https://example.com/updated.png")
                .createdAt(testSkin.getCreatedAt())
                .updatedAt(updatedSkin.getUpdatedAt())
                .build();

        when(skinRepository.findById(skinId)).thenReturn(Optional.of(testSkin));
        when(skinRepository.save(any(Skin.class))).thenReturn(updatedSkin);

        SkinResponse response = skinService.update(ownerId, skinId, request);

        assertNotNull(response);
        assertEquals("AWP | Dragon Lore (Updated)", response.getName());
        assertEquals(Wear.MINIMAL_WEAR, response.getWear());

        verify(skinRepository, times(1)).findById(skinId);
        verify(skinRepository, times(1)).save(any(Skin.class));
    }

    @Test
    void testUpdateSkin_NotFound() {
        UpdateSkinRequest request = UpdateSkinRequest.builder()
                .name("Updated")
                .weapon("AWP")
                .rarity(Rarity.COVERT)
                .wear(Wear.FACTORY_NEW)
                .floatValue(new BigDecimal("0.01"))
                .build();

        when(skinRepository.findById(skinId)).thenReturn(Optional.empty());

        assertThrows(SkinNotFoundException.class, () -> skinService.update(ownerId, skinId, request));

        verify(skinRepository, times(1)).findById(skinId);
    }

    @Test
    void testUpdateSkin_AccessDenied() {
        UUID anotherUserId = UUID.randomUUID();
        UpdateSkinRequest request = UpdateSkinRequest.builder()
                .name("Updated")
                .weapon("AWP")
                .rarity(Rarity.COVERT)
                .wear(Wear.FACTORY_NEW)
                .floatValue(new BigDecimal("0.01"))
                .build();

        when(skinRepository.findById(skinId)).thenReturn(Optional.of(testSkin));

        assertThrows(SkinAccessDeniedException.class, () -> skinService.update(anotherUserId, skinId, request));

        verify(skinRepository, times(1)).findById(skinId);
    }

    @Test
    void testDeleteSkin_Success() {
        when(skinRepository.findById(skinId)).thenReturn(Optional.of(testSkin));

        skinService.delete(ownerId, skinId);

        verify(skinRepository, times(1)).findById(skinId);
        verify(skinRepository, times(1)).deleteById(skinId);
    }

    @Test
    void testDeleteSkin_NotFound() {
        when(skinRepository.findById(skinId)).thenReturn(Optional.empty());

        assertThrows(SkinNotFoundException.class, () -> skinService.delete(ownerId, skinId));

        verify(skinRepository, times(1)).findById(skinId);
        verify(skinRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteSkin_AccessDenied() {
        UUID anotherUserId = UUID.randomUUID();
        when(skinRepository.findById(skinId)).thenReturn(Optional.of(testSkin));

        assertThrows(SkinAccessDeniedException.class, () -> skinService.delete(anotherUserId, skinId));

        verify(skinRepository, times(1)).findById(skinId);
        verify(skinRepository, never()).deleteById(any());
    }
}
