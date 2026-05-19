package com.storrego.catalog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storrego.catalog.dto.BulkCreateResponse;
import com.storrego.catalog.dto.CatalogSkinDetailResponse;
import com.storrego.catalog.dto.PagedResponse;
import com.storrego.catalog.entity.CatalogSkin;
import com.storrego.catalog.exception.CatalogSkinNotFoundException;
import com.storrego.catalog.exception.DuplicateCatalogSkinException;
import com.storrego.catalog.exception.InvalidCatalogSkinJsonException;
import com.storrego.catalog.mapper.CatalogSkinMapper;
import com.storrego.catalog.repository.CatalogSkinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentMatchers;

@ExtendWith(MockitoExtension.class)
class CatalogSkinServiceTest {

    @Mock
    private CatalogSkinRepository repository;

    @Spy
    private CatalogSkinMapper mapper;

    @InjectMocks
    private CatalogSkinService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
              "wears": [],
              "collections": [],
              "crates": [],
              "team": { "id": "both", "name": "Both Teams" },
              "legacy_model": false,
              "image": "https://example.com/skin.png",
              "original": { "name": "leather_handwraps" }
            }
            """;

    private JsonNode handWrapsNode;
    private CatalogSkin handWrapsSkin;

    @BeforeEach
    void setUp() throws Exception {
        handWrapsNode = objectMapper.readTree(HAND_WRAPS_JSON);
        handWrapsSkin = CatalogSkin.builder()
                .id("skin-e757fd7191f9")
                .name("★ Hand Wraps | Spruce DDPAT")
                .description("Preferred by hand-to-hand fighters...")
                .image("https://example.com/skin.png")
                .weaponId("leather_handwraps")
                .weaponName("Hand Wraps")
                .categoryId("sfui_invpanel_filter_gloves")
                .categoryName("Gloves")
                .rarityId("rarity_ancient")
                .rarityName("Extraordinary")
                .rarityColor("#eb4b4b")
                .minFloat(new BigDecimal("0.06"))
                .maxFloat(new BigDecimal("0.8"))
                .stattrak(false)
                .souvenir(false)
                .paintIndex("10010")
                .legacyModel(false)
                .rawData(handWrapsNode)
                .build();
    }

    // --- list ---

    @Test
    void list_noFilters_returnsPagedResponse() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("name"));
        Page<CatalogSkin> page = new PageImpl<>(List.of(handWrapsSkin), pageable, 1);
        Specification<CatalogSkin> anySpec = ArgumentMatchers.any();
        when(repository.findAll(anySpec, any(Pageable.class))).thenReturn(page);

        PagedResponse<?> response = service.list(null, null, null, null, null, null, pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    void list_maxSizeEnforced() {
        Pageable oversized = PageRequest.of(0, 500, Sort.by("name"));
        Page<CatalogSkin> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 100, Sort.by("name")), 0);
        Specification<CatalogSkin> anySpec = ArgumentMatchers.any();
        when(repository.findAll(anySpec, any(Pageable.class))).thenReturn(emptyPage);

        service.list(null, null, null, null, null, null, oversized);

        Specification<CatalogSkin> anySpec2 = ArgumentMatchers.any();
        verify(repository).findAll(anySpec2, ArgumentMatchers.<Pageable>argThat(p -> p.getPageSize() == 100));
    }

    // --- getById ---

    @Test
    void getById_found_returnsDetail() {
        when(repository.findById("skin-e757fd7191f9")).thenReturn(Optional.of(handWrapsSkin));

        CatalogSkinDetailResponse response = service.getById("skin-e757fd7191f9");

        assertThat(response.getId()).isEqualTo("skin-e757fd7191f9");
        assertThat(response.getWeaponName()).isEqualTo("Hand Wraps");
        assertThat(response.getRawData()).isNotNull();
    }

    @Test
    void getById_notFound_throwsNotFoundException() {
        when(repository.findById("skin-nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("skin-nope"))
                .isInstanceOf(CatalogSkinNotFoundException.class)
                .hasMessageContaining("skin-nope");
    }

    // --- create ---

    @Test
    void create_success_returnsSavedDetail() {
        when(repository.existsById("skin-e757fd7191f9")).thenReturn(false);
        when(repository.save(any(CatalogSkin.class))).thenReturn(handWrapsSkin);

        CatalogSkinDetailResponse response = service.create(handWrapsNode);

        assertThat(response.getId()).isEqualTo("skin-e757fd7191f9");
        assertThat(response.getCategoryName()).isEqualTo("Gloves");
        verify(repository).save(any(CatalogSkin.class));
    }

    @Test
    void create_duplicateId_throwsDuplicateException() {
        when(repository.existsById("skin-e757fd7191f9")).thenReturn(true);

        assertThatThrownBy(() -> service.create(handWrapsNode))
                .isInstanceOf(DuplicateCatalogSkinException.class)
                .hasMessageContaining("skin-e757fd7191f9");
        verify(repository, never()).save(any());
    }

    @Test
    void create_missingId_throwsInvalidJsonException() throws Exception {
        JsonNode noId = objectMapper.readTree("{\"name\":\"No ID Skin\"}");

        assertThatThrownBy(() -> service.create(noId))
                .isInstanceOf(InvalidCatalogSkinJsonException.class)
                .hasMessageContaining("'id'");
    }

    @Test
    void create_missingName_throwsInvalidJsonException() throws Exception {
        JsonNode noName = objectMapper.readTree("{\"id\":\"skin-abc\"}");

        assertThatThrownBy(() -> service.create(noName))
                .isInstanceOf(InvalidCatalogSkinJsonException.class)
                .hasMessageContaining("'name'");
    }

    // --- bulkCreate ---

    @Test
    void bulkCreate_allSuccess_returnsCorrectCounts() {
        when(repository.existsById("skin-e757fd7191f9")).thenReturn(false);
        when(repository.save(any(CatalogSkin.class))).thenReturn(handWrapsSkin);

        BulkCreateResponse response = service.bulkCreate(List.of(handWrapsNode));

        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getCreated()).isEqualTo(1);
        assertThat(response.getSkipped()).isEqualTo(0);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void bulkCreate_partialSkip_recordsErrors() throws Exception {
        JsonNode noId = objectMapper.readTree("{\"name\":\"Bad Skin\"}");
        when(repository.existsById("skin-e757fd7191f9")).thenReturn(true);

        BulkCreateResponse response = service.bulkCreate(List.of(handWrapsNode, noId));

        assertThat(response.getTotal()).isEqualTo(2);
        assertThat(response.getCreated()).isEqualTo(0);
        assertThat(response.getSkipped()).isEqualTo(2);
        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors().get(0).getReason()).isEqualTo("Already exists");
    }

    // --- update ---

    @Test
    void update_success_returnsUpdatedDetail() {
        when(repository.findById("skin-e757fd7191f9")).thenReturn(Optional.of(handWrapsSkin));
        when(repository.save(any(CatalogSkin.class))).thenReturn(handWrapsSkin);

        CatalogSkinDetailResponse response = service.update("skin-e757fd7191f9", handWrapsNode);

        assertThat(response.getId()).isEqualTo("skin-e757fd7191f9");
        verify(repository).save(handWrapsSkin);
    }

    @Test
    void update_notFound_throwsNotFoundException() {
        when(repository.findById("skin-nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update("skin-nope", handWrapsNode))
                .isInstanceOf(CatalogSkinNotFoundException.class);
        verify(repository, never()).save(any());
    }

    // --- delete ---

    @Test
    void delete_success_callsDeleteById() {
        when(repository.existsById("skin-e757fd7191f9")).thenReturn(true);

        service.delete("skin-e757fd7191f9");

        verify(repository).deleteById("skin-e757fd7191f9");
    }

    @Test
    void delete_notFound_throwsNotFoundException() {
        when(repository.existsById("skin-nope")).thenReturn(false);

        assertThatThrownBy(() -> service.delete("skin-nope"))
                .isInstanceOf(CatalogSkinNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }

    // --- mapJsonToEntity (nulls / optional fields) ---

    @Test
    void mapJsonToEntity_nullableFieldsMissing_doesNotThrow() throws Exception {
        JsonNode minimal = objectMapper.readTree("{\"id\":\"skin-min\",\"name\":\"Minimal Skin\"}");

        CatalogSkin skin = service.mapJsonToEntity(minimal);

        assertThat(skin.getId()).isEqualTo("skin-min");
        assertThat(skin.getWeaponName()).isNull();
        assertThat(skin.getRarityColor()).isNull();
        assertThat(skin.getMinFloat()).isNull();
        assertThat(skin.getStattrak()).isFalse();
        assertThat(skin.getSouvenir()).isFalse();
        assertThat(skin.getLegacyModel()).isFalse();
    }

    @Test
    void mapJsonToEntity_handWraps_mapsAllFields() {
        CatalogSkin skin = service.mapJsonToEntity(handWrapsNode);

        assertThat(skin.getId()).isEqualTo("skin-e757fd7191f9");
        assertThat(skin.getName()).isEqualTo("★ Hand Wraps | Spruce DDPAT");
        assertThat(skin.getWeaponId()).isEqualTo("leather_handwraps");
        assertThat(skin.getWeaponName()).isEqualTo("Hand Wraps");
        assertThat(skin.getCategoryId()).isEqualTo("sfui_invpanel_filter_gloves");
        assertThat(skin.getCategoryName()).isEqualTo("Gloves");
        assertThat(skin.getRarityId()).isEqualTo("rarity_ancient");
        assertThat(skin.getRarityName()).isEqualTo("Extraordinary");
        assertThat(skin.getRarityColor()).isEqualTo("#eb4b4b");
        assertThat(skin.getMinFloat()).isEqualByComparingTo("0.06");
        assertThat(skin.getMaxFloat()).isEqualByComparingTo("0.8");
        assertThat(skin.getPaintIndex()).isEqualTo("10010");
        assertThat(skin.getStattrak()).isFalse();
        assertThat(skin.getSouvenir()).isFalse();
        assertThat(skin.getLegacyModel()).isFalse();
        assertThat(skin.getRawData()).isNotNull();
    }
}
