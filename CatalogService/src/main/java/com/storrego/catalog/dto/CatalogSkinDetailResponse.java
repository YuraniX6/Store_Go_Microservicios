package com.storrego.catalog.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class CatalogSkinDetailResponse {
    String id;
    String name;
    String description;
    String image;
    String weaponName;
    String categoryName;
    String rarityName;
    String rarityColor;
    BigDecimal minFloat;
    BigDecimal maxFloat;
    Boolean stattrak;
    Boolean souvenir;
    String paintIndex;
    Boolean legacyModel;
    JsonNode rawData;
    Instant createdAt;
    Instant updatedAt;
}
