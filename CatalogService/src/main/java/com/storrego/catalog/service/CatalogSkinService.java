package com.storrego.catalog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.storrego.catalog.dto.BulkCreateResponse;
import com.storrego.catalog.dto.CatalogSkinDetailResponse;
import com.storrego.catalog.dto.CatalogSkinSummaryResponse;
import com.storrego.catalog.dto.PagedResponse;
import com.storrego.catalog.entity.CatalogSkin;
import com.storrego.catalog.exception.CatalogSkinNotFoundException;
import com.storrego.catalog.exception.DuplicateCatalogSkinException;
import com.storrego.catalog.exception.InvalidCatalogSkinJsonException;
import com.storrego.catalog.mapper.CatalogSkinMapper;
import com.storrego.catalog.repository.CatalogSkinRepository;
import com.storrego.catalog.repository.CatalogSkinSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogSkinService {

    private static final int MAX_PAGE_SIZE = 100;

    private final CatalogSkinRepository repository;
    private final CatalogSkinMapper mapper;

    @Transactional(readOnly = true)
    public PagedResponse<CatalogSkinSummaryResponse> list(
            String weapon, String category, String rarity,
            Boolean stattrak, Boolean souvenir, String q,
            Pageable pageable) {

        int size = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable effective = PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
        Specification<CatalogSkin> spec = buildSpec(weapon, category, rarity, stattrak, souvenir, q);
        Page<CatalogSkin> page = repository.findAll(spec, effective);

        List<CatalogSkinSummaryResponse> content = page.getContent().stream()
                .map(mapper::toSummary)
                .toList();

        return PagedResponse.<CatalogSkinSummaryResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CatalogSkinSummaryResponse> listAll() {
        return repository.findAll(Sort.by("name").ascending()).stream()
                .map(mapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogSkinDetailResponse getById(String id) {
        return repository.findById(id)
                .map(mapper::toDetail)
                .orElseThrow(() -> new CatalogSkinNotFoundException(id));
    }

    @Transactional
    public CatalogSkinDetailResponse create(JsonNode json) {
        CatalogSkin skin = mapJsonToEntity(json);
        if (repository.existsById(skin.getId())) {
            throw new DuplicateCatalogSkinException(skin.getId());
        }
        CatalogSkin saved = repository.save(skin);
        log.info("Created catalog skin: {}", saved.getId());
        return mapper.toDetail(saved);
    }

    public BulkCreateResponse bulkCreate(List<JsonNode> skins) {
        int total = skins.size();
        int created = 0;
        int skipped = 0;
        List<BulkCreateResponse.BulkError> errors = new ArrayList<>();

        for (int i = 0; i < skins.size(); i++) {
            JsonNode json = skins.get(i);
            String skinId = null;
            try {
                CatalogSkin skin = mapJsonToEntity(json);
                skinId = skin.getId();
                if (repository.existsById(skinId)) {
                    skipped++;
                    errors.add(BulkCreateResponse.BulkError.builder()
                            .index(i).id(skinId).reason("Already exists").build());
                } else {
                    repository.save(skin);
                    created++;
                }
            } catch (InvalidCatalogSkinJsonException e) {
                skipped++;
                errors.add(BulkCreateResponse.BulkError.builder()
                        .index(i).id(skinId).reason(e.getMessage()).build());
            } catch (Exception e) {
                skipped++;
                log.error("Unexpected error at bulk index {}: {}", i, e.getMessage());
                errors.add(BulkCreateResponse.BulkError.builder()
                        .index(i).id(skinId).reason(e.getMessage()).build());
            }
        }

        log.info("Bulk create: total={}, created={}, skipped={}", total, created, skipped);
        return BulkCreateResponse.builder()
                .total(total).created(created).skipped(skipped).errors(errors).build();
    }

    @Transactional
    public CatalogSkinDetailResponse update(String id, JsonNode json) {
        CatalogSkin existing = repository.findById(id)
                .orElseThrow(() -> new CatalogSkinNotFoundException(id));
        applyJsonFields(json, existing);
        CatalogSkin saved = repository.save(existing);
        log.info("Updated catalog skin: {}", saved.getId());
        return mapper.toDetail(saved);
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new CatalogSkinNotFoundException(id);
        }
        repository.deleteById(id);
        log.info("Deleted catalog skin: {}", id);
    }

    CatalogSkin mapJsonToEntity(JsonNode json) {
        String id = textOrNull(json, "id");
        if (id == null || id.isBlank()) {
            throw new InvalidCatalogSkinJsonException("Field 'id' is required and cannot be blank");
        }
        CatalogSkin skin = new CatalogSkin();
        skin.setId(id);
        applyJsonFields(json, skin);
        return skin;
    }

    private void applyJsonFields(JsonNode json, CatalogSkin skin) {
        String name = textOrNull(json, "name");
        if (name == null || name.isBlank()) {
            throw new InvalidCatalogSkinJsonException("Field 'name' is required and cannot be blank");
        }

        JsonNode weapon   = json.path("weapon");
        JsonNode category = json.path("category");
        JsonNode rarity   = json.path("rarity");

        skin.setName(name);
        skin.setDescription(textOrNull(json, "description"));
        skin.setImage(textOrNull(json, "image"));
        skin.setWeaponId(nestedTextOrNull(weapon, "id"));
        skin.setWeaponName(nestedTextOrNull(weapon, "name"));
        skin.setCategoryId(nestedTextOrNull(category, "id"));
        skin.setCategoryName(nestedTextOrNull(category, "name"));
        skin.setRarityId(nestedTextOrNull(rarity, "id"));
        skin.setRarityName(nestedTextOrNull(rarity, "name"));
        skin.setRarityColor(nestedTextOrNull(rarity, "color"));
        skin.setMinFloat(decimalOrNull(json, "min_float"));
        skin.setMaxFloat(decimalOrNull(json, "max_float"));
        skin.setStattrak(boolOrDefault(json, "stattrak", false));
        skin.setSouvenir(boolOrDefault(json, "souvenir", false));
        skin.setPaintIndex(textOrNull(json, "paint_index"));
        skin.setLegacyModel(boolOrDefault(json, "legacy_model", false));
        skin.setRawData(json);
    }

    private Specification<CatalogSkin> buildSpec(
            String weapon, String category, String rarity,
            Boolean stattrak, Boolean souvenir, String q) {

        Specification<CatalogSkin> spec = Specification.where(null);
        if (weapon   != null && !weapon.isBlank())   spec = spec.and(CatalogSkinSpecifications.weaponNameEquals(weapon));
        if (category != null && !category.isBlank()) spec = spec.and(CatalogSkinSpecifications.categoryNameEquals(category));
        if (rarity   != null && !rarity.isBlank())   spec = spec.and(CatalogSkinSpecifications.rarityNameEquals(rarity));
        if (stattrak != null)                         spec = spec.and(CatalogSkinSpecifications.stattrakEquals(stattrak));
        if (souvenir != null)                         spec = spec.and(CatalogSkinSpecifications.souvenirEquals(souvenir));
        if (q        != null && !q.isBlank())         spec = spec.and(CatalogSkinSpecifications.nameLike(q));
        return spec;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v != null && !v.isNull()) ? v.asText(null) : null;
    }

    private String nestedTextOrNull(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        return textOrNull(node, field);
    }

    private BigDecimal decimalOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull() || !v.isNumber()) return null;
        try {
            return v.decimalValue();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean boolOrDefault(JsonNode node, String field, boolean defaultValue) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return defaultValue;
        return v.asBoolean(defaultValue);
    }
}
