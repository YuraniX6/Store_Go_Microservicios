package com.storego.inventoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storego.inventoryservice.dto.CreateSkinRequest;
import com.storego.inventoryservice.dto.SkinResponse;
import com.storego.inventoryservice.dto.UpdateSkinRequest;
import com.storego.inventoryservice.entity.Rarity;
import com.storego.inventoryservice.entity.Wear;
import com.storego.inventoryservice.exception.SkinAccessDeniedException;
import com.storego.inventoryservice.exception.SkinNotFoundException;
import com.storego.inventoryservice.service.SkinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SkinController.class)
class SkinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SkinService skinService;

    private UUID ownerId;
    private UUID skinId;
    private SkinResponse testSkinResponse;
    private CreateSkinRequest createRequest;

    @BeforeEach
    void setUp() {
        ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        skinId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

        testSkinResponse = SkinResponse.builder()
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

        createRequest = CreateSkinRequest.builder()
                .name("AWP | Dragon Lore")
                .weapon("AWP")
                .rarity(Rarity.COVERT)
                .wear(Wear.FACTORY_NEW)
                .floatValue(new BigDecimal("0.012345"))
                .imageUrl("https://example.com/skin.png")
                .build();
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testCreateSkin_Success() throws Exception {
        when(skinService.create(eq(ownerId), any(CreateSkinRequest.class)))
                .thenReturn(testSkinResponse);

        mockMvc.perform(post("/skins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(skinId.toString()))
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
                .andExpect(jsonPath("$.name").value("AWP | Dragon Lore"))
                .andExpect(jsonPath("$.weapon").value("AWP"));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testCreateSkin_ValidationFails() throws Exception {
        CreateSkinRequest invalidRequest = CreateSkinRequest.builder()
                .name("")
                .weapon("")
                .rarity(Rarity.COVERT)
                .wear(Wear.FACTORY_NEW)
                .floatValue(new BigDecimal("2.0"))
                .build();

        mockMvc.perform(post("/skins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testGetMySkins_Success() throws Exception {
        SkinResponse skin2 = SkinResponse.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"))
                .ownerId(ownerId)
                .name("AK-47 | Redline")
                .weapon("AK-47")
                .rarity(Rarity.CLASSIFIED)
                .wear(Wear.MINIMAL_WEAR)
                .floatValue(new BigDecimal("0.25"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(skinService.getMySkins(ownerId))
                .thenReturn(Arrays.asList(testSkinResponse, skin2));

        mockMvc.perform(get("/skins/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("AWP | Dragon Lore"))
                .andExpect(jsonPath("$[1].name").value("AK-47 | Redline"));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testGetMySkins_Empty() throws Exception {
        when(skinService.getMySkins(ownerId))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/skins/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testGetSkin_Success() throws Exception {
        when(skinService.getSkin(ownerId, skinId))
                .thenReturn(testSkinResponse);

        mockMvc.perform(get("/skins/" + skinId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(skinId.toString()))
                .andExpect(jsonPath("$.name").value("AWP | Dragon Lore"));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testGetSkin_NotFound() throws Exception {
        when(skinService.getSkin(ownerId, skinId))
                .thenThrow(new SkinNotFoundException("Skin not found"));

        mockMvc.perform(get("/skins/" + skinId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testGetSkin_AccessDenied() throws Exception {
        when(skinService.getSkin(ownerId, skinId))
                .thenThrow(new SkinAccessDeniedException("You do not have permission to access this skin"));

        mockMvc.perform(get("/skins/" + skinId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testGetSkin_InvalidUuid() throws Exception {
        mockMvc.perform(get("/skins/invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testUpdateSkin_Success() throws Exception {
        UpdateSkinRequest updateRequest = UpdateSkinRequest.builder()
                .name("AWP | Dragon Lore (Updated)")
                .weapon("AWP")
                .rarity(Rarity.COVERT)
                .wear(Wear.MINIMAL_WEAR)
                .floatValue(new BigDecimal("0.02"))
                .imageUrl("https://example.com/updated.png")
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
                .createdAt(testSkinResponse.getCreatedAt())
                .updatedAt(Instant.now())
                .build();

        when(skinService.update(eq(ownerId), eq(skinId), any(UpdateSkinRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/skins/" + skinId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("AWP | Dragon Lore (Updated)"))
                .andExpect(jsonPath("$.wear").value("MINIMAL_WEAR"));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testUpdateSkin_NotFound() throws Exception {
        UpdateSkinRequest updateRequest = UpdateSkinRequest.builder()
                .name("Updated")
                .weapon("AWP")
                .rarity(Rarity.COVERT)
                .wear(Wear.FACTORY_NEW)
                .floatValue(new BigDecimal("0.01"))
                .build();

        when(skinService.update(eq(ownerId), eq(skinId), any(UpdateSkinRequest.class)))
                .thenThrow(new SkinNotFoundException("Skin not found"));

        mockMvc.perform(put("/skins/" + skinId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testUpdateSkin_AccessDenied() throws Exception {
        UpdateSkinRequest updateRequest = UpdateSkinRequest.builder()
                .name("Updated")
                .weapon("AWP")
                .rarity(Rarity.COVERT)
                .wear(Wear.FACTORY_NEW)
                .floatValue(new BigDecimal("0.01"))
                .build();

        when(skinService.update(eq(ownerId), eq(skinId), any(UpdateSkinRequest.class)))
                .thenThrow(new SkinAccessDeniedException("You do not have permission to access this skin"));

        mockMvc.perform(put("/skins/" + skinId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testDeleteSkin_Success() throws Exception {
        mockMvc.perform(delete("/skins/" + skinId)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testDeleteSkin_NotFound() throws Exception {
        doThrow(new SkinNotFoundException("Skin not found"))
                .when(skinService).delete(ownerId, skinId);

        mockMvc.perform(delete("/skins/" + skinId)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testDeleteSkin_AccessDenied() throws Exception {
        doThrow(new SkinAccessDeniedException("You do not have permission to access this skin"))
                .when(skinService).delete(ownerId, skinId);

        mockMvc.perform(delete("/skins/" + skinId)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateSkin_Unauthorized() throws Exception {
        mockMvc.perform(post("/skins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
