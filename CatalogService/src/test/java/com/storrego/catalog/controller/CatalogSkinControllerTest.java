package com.storrego.catalog.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storrego.catalog.dto.BulkCreateResponse;
import com.storrego.catalog.dto.CatalogSkinDetailResponse;
import com.storrego.catalog.dto.CatalogSkinSummaryResponse;
import com.storrego.catalog.dto.PagedResponse;
import com.storrego.catalog.exception.CatalogSkinNotFoundException;
import com.storrego.catalog.exception.DuplicateCatalogSkinException;
import com.storrego.catalog.security.JwtService;
import com.storrego.catalog.service.CatalogSkinService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CatalogSkinController.class)
class CatalogSkinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CatalogSkinService service;

    @MockBean
    private JwtService jwtService;

    private static final String SKIN_ID = "skin-e757fd7191f9";

    private static final String HAND_WRAPS_JSON = """
            {
              "id": "skin-e757fd7191f9",
              "name": "★ Hand Wraps | Spruce DDPAT",
              "description": "Preferred by hand-to-hand fighters...",
              "weapon": { "id": "leather_handwraps", "weapon_id": 5032, "name": "Hand Wraps" },
              "category": { "id": "sfui_invpanel_filter_gloves", "name": "Gloves" },
              "pattern": { "id": "handwrap_camo_grey", "name": "Spruce DDPAT" },
              "min_float": 0.06,
              "max_float": 0.8,
              "rarity": { "id": "rarity_ancient", "name": "Extraordinary", "color": "#eb4b4b" },
              "stattrak": false,
              "souvenir": false,
              "paint_index": "10010",
              "wears": [{ "id": "SFUI_InvTooltip_Wear_Amount_0", "name": "Factory New" }],
              "collections": [],
              "crates": [{ "id": "crate-4288", "name": "Glove Case", "image": "https://example.com/img.png" }],
              "team": { "id": "both", "name": "Both Teams" },
              "legacy_model": false,
              "image": "https://example.com/skin.png",
              "original": { "name": "leather_handwraps" }
            }
            """;

    private CatalogSkinSummaryResponse summaryResponse;
    private CatalogSkinDetailResponse detailResponse;

    @BeforeEach
    void setUp() throws Exception {
        JsonNode rawData = objectMapper.readTree(HAND_WRAPS_JSON);

        summaryResponse = CatalogSkinSummaryResponse.builder()
                .id(SKIN_ID)
                .name("★ Hand Wraps | Spruce DDPAT")
                .image("https://example.com/skin.png")
                .weaponName("Hand Wraps")
                .categoryName("Gloves")
                .rarityName("Extraordinary")
                .rarityColor("#eb4b4b")
                .stattrak(false)
                .souvenir(false)
                .build();

        detailResponse = CatalogSkinDetailResponse.builder()
                .id(SKIN_ID)
                .name("★ Hand Wraps | Spruce DDPAT")
                .description("Preferred by hand-to-hand fighters...")
                .image("https://example.com/skin.png")
                .weaponName("Hand Wraps")
                .categoryName("Gloves")
                .rarityName("Extraordinary")
                .rarityColor("#eb4b4b")
                .minFloat(new BigDecimal("0.06"))
                .maxFloat(new BigDecimal("0.8"))
                .stattrak(false)
                .souvenir(false)
                .paintIndex("10010")
                .legacyModel(false)
                .rawData(rawData)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // --- GET /catalog/skins ---

    @Test
    @WithMockUser(authorities = "USER")
    void listSkins_asUser_returns200() throws Exception {
        PagedResponse<CatalogSkinSummaryResponse> page = PagedResponse.<CatalogSkinSummaryResponse>builder()
                .content(List.of(summaryResponse))
                .page(0).size(20).totalElements(1).totalPages(1).first(true).last(true)
                .build();
        when(service.list(any(), any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/catalog/skins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(SKIN_ID))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void listSkins_asAdmin_returns200() throws Exception {
        PagedResponse<CatalogSkinSummaryResponse> page = PagedResponse.<CatalogSkinSummaryResponse>builder()
                .content(List.of()).page(0).size(20).totalElements(0).totalPages(0).first(true).last(true)
                .build();
        when(service.list(any(), any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/catalog/skins").param("weapon", "Hand Wraps").param("stattrak", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void listSkins_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/catalog/skins"))
                .andExpect(status().isUnauthorized());
    }

    // --- GET /catalog/skins/{id} ---

    @Test
    @WithMockUser(authorities = "USER")
    void getSkinById_found_returns200() throws Exception {
        when(service.getById(SKIN_ID)).thenReturn(detailResponse);

        mockMvc.perform(get("/catalog/skins/{id}", SKIN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SKIN_ID))
                .andExpect(jsonPath("$.rawData.weapon.name").value("Hand Wraps"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void getSkinById_notFound_returns404() throws Exception {
        when(service.getById("skin-unknown")).thenThrow(new CatalogSkinNotFoundException("skin-unknown"));

        mockMvc.perform(get("/catalog/skins/{id}", "skin-unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // --- POST /catalog/skins ---

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createSkin_asAdmin_returns201() throws Exception {
        when(service.create(any(JsonNode.class))).thenReturn(detailResponse);

        mockMvc.perform(post("/catalog/skins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HAND_WRAPS_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(SKIN_ID));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void createSkin_asUser_returns403() throws Exception {
        mockMvc.perform(post("/catalog/skins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HAND_WRAPS_JSON))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createSkin_duplicate_returns409() throws Exception {
        when(service.create(any(JsonNode.class))).thenThrow(new DuplicateCatalogSkinException(SKIN_ID));

        mockMvc.perform(post("/catalog/skins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HAND_WRAPS_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createSkin_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/catalog/skins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    // --- POST /catalog/skins/bulk ---

    @Test
    @WithMockUser(authorities = "ADMIN")
    void bulkCreate_asAdmin_returns200() throws Exception {
        BulkCreateResponse bulkResponse = BulkCreateResponse.builder()
                .total(1).created(1).skipped(0).errors(List.of()).build();
        when(service.bulkCreate(anyList())).thenReturn(bulkResponse);

        String body = "{\"skins\":[" + HAND_WRAPS_JSON + "]}";
        mockMvc.perform(post("/catalog/skins/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.created").value(1));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void bulkCreate_asUser_returns403() throws Exception {
        String body = "{\"skins\":[" + HAND_WRAPS_JSON + "]}";
        mockMvc.perform(post("/catalog/skins/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void bulkCreate_emptyList_returns400() throws Exception {
        mockMvc.perform(post("/catalog/skins/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skins\":[]}"))
                .andExpect(status().isBadRequest());
    }

    // --- PUT /catalog/skins/{id} ---

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateSkin_asAdmin_returns200() throws Exception {
        when(service.update(eq(SKIN_ID), any(JsonNode.class))).thenReturn(detailResponse);

        mockMvc.perform(put("/catalog/skins/{id}", SKIN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HAND_WRAPS_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SKIN_ID));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void updateSkin_asUser_returns403() throws Exception {
        mockMvc.perform(put("/catalog/skins/{id}", SKIN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HAND_WRAPS_JSON))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateSkin_notFound_returns404() throws Exception {
        when(service.update(eq("skin-nope"), any(JsonNode.class)))
                .thenThrow(new CatalogSkinNotFoundException("skin-nope"));

        mockMvc.perform(put("/catalog/skins/{id}", "skin-nope")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HAND_WRAPS_JSON))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /catalog/skins/{id} ---

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteSkin_asAdmin_returns204() throws Exception {
        doNothing().when(service).delete(SKIN_ID);

        mockMvc.perform(delete("/catalog/skins/{id}", SKIN_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void deleteSkin_asUser_returns403() throws Exception {
        mockMvc.perform(delete("/catalog/skins/{id}", SKIN_ID))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteSkin_notFound_returns404() throws Exception {
        doThrow(new CatalogSkinNotFoundException(SKIN_ID)).when(service).delete(SKIN_ID);

        mockMvc.perform(delete("/catalog/skins/{id}", SKIN_ID))
                .andExpect(status().isNotFound());
    }
}
