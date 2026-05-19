package com.storrego.catalog.mapper;

import com.storrego.catalog.dto.CatalogSkinDetailResponse;
import com.storrego.catalog.dto.CatalogSkinSummaryResponse;
import com.storrego.catalog.entity.CatalogSkin;
import org.springframework.stereotype.Component;

@Component
public class CatalogSkinMapper {

    public CatalogSkinSummaryResponse toSummary(CatalogSkin skin) {
        return CatalogSkinSummaryResponse.builder()
                .id(skin.getId())
                .name(skin.getName())
                .image(skin.getImage())
                .weaponName(skin.getWeaponName())
                .categoryName(skin.getCategoryName())
                .rarityName(skin.getRarityName())
                .rarityColor(skin.getRarityColor())
                .stattrak(skin.getStattrak())
                .souvenir(skin.getSouvenir())
                .build();
    }

    public CatalogSkinDetailResponse toDetail(CatalogSkin skin) {
        return CatalogSkinDetailResponse.builder()
                .id(skin.getId())
                .name(skin.getName())
                .description(skin.getDescription())
                .image(skin.getImage())
                .weaponName(skin.getWeaponName())
                .categoryName(skin.getCategoryName())
                .rarityName(skin.getRarityName())
                .rarityColor(skin.getRarityColor())
                .minFloat(skin.getMinFloat())
                .maxFloat(skin.getMaxFloat())
                .stattrak(skin.getStattrak())
                .souvenir(skin.getSouvenir())
                .paintIndex(skin.getPaintIndex())
                .legacyModel(skin.getLegacyModel())
                .rawData(skin.getRawData())
                .createdAt(skin.getCreatedAt())
                .updatedAt(skin.getUpdatedAt())
                .build();
    }
}
