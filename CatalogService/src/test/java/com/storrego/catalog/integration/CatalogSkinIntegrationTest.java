package com.storrego.catalog.integration;

import com.storrego.catalog.repository.CatalogSkinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CatalogSkinIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("catalog_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("jwt.secret", () -> "dGVzdC1zZWNyZXQtZm9yLXRlc3RpbmctcHVycG9zZXMtb25seS1taW5pbXVtLTI1Ni1iaXRz");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CatalogSkinRepository repository;

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

    @BeforeEach
    void cleanDb() {
        repository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void fullCrudFlow() throws Exception {
        // POST → 201
        mockMvc.perform(post("/catalog/skins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HAND_WRAPS_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(SKIN_ID))
                .andExpect(jsonPath("$.weaponName").value("Hand Wraps"))
                .andExpect(jsonPath("$.rawData.rarity.name").value("Extraordinary"));

        // GET list → 200, skin appears
        mockMvc.perform(get("/catalog/skins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(SKIN_ID));

        // GET by id → 200 with rawData
        mockMvc.perform(get("/catalog/skins/{id}", SKIN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SKIN_ID))
                .andExpect(jsonPath("$.rawData.weapon.name").value("Hand Wraps"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        // PUT → 200
        mockMvc.perform(put("/catalog/skins/{id}", SKIN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HAND_WRAPS_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SKIN_ID));

        // DELETE → 204
        mockMvc.perform(delete("/catalog/skins/{id}", SKIN_ID))
                .andExpect(status().isNoContent());

        // GET by id after delete → 404
        mockMvc.perform(get("/catalog/skins/{id}", SKIN_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void postDuplicate_returns409() throws Exception {
        mockMvc.perform(post("/catalog/skins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HAND_WRAPS_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/catalog/skins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HAND_WRAPS_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void userCannotCreate_returns403() throws Exception {
        mockMvc.perform(post("/catalog/skins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HAND_WRAPS_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void userCannotDelete_returns403() throws Exception {
        mockMvc.perform(delete("/catalog/skins/{id}", SKIN_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void bulkCreate_returns200WithCounts() throws Exception {
        String bulk = "{\"skins\":[" + HAND_WRAPS_JSON + "]}";

        mockMvc.perform(post("/catalog/skins/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulk))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.created").value(1))
                .andExpect(jsonPath("$.skipped").value(0));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void listFilters_byWeapon_returnsFilteredResults() throws Exception {
        mockMvc.perform(post("/catalog/skins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HAND_WRAPS_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/catalog/skins")
                        .param("weapon", "Hand Wraps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/catalog/skins")
                        .param("weapon", "AK-47"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void listFilters_byQuery_returnsMatchingResults() throws Exception {
        mockMvc.perform(post("/catalog/skins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HAND_WRAPS_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/catalog/skins").param("q", "spruce"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/catalog/skins").param("q", "karambit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/catalog/skins"))
                .andExpect(status().isUnauthorized());
    }
}
