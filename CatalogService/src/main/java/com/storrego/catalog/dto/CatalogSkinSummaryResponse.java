package com.storrego.catalog.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CatalogSkinSummaryResponse {
    String id;
    String name;
    String image;
    String weaponName;
    String categoryName;
    String rarityName;
    String rarityColor;
    Boolean stattrak;
    Boolean souvenir;
}
