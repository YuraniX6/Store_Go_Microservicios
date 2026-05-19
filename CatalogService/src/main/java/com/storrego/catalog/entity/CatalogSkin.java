package com.storrego.catalog.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "catalog_skins",
    indexes = {
        @Index(name = "idx_catalog_skins_weapon_name",   columnList = "weapon_name"),
        @Index(name = "idx_catalog_skins_category_name", columnList = "category_name"),
        @Index(name = "idx_catalog_skins_rarity_name",   columnList = "rarity_name"),
        @Index(name = "idx_catalog_skins_name_lower",    columnList = "name")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogSkin {

    @Id
    @Column(name = "id", length = 50, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image", length = 500)
    private String image;

    @Column(name = "weapon_id", length = 50)
    private String weaponId;

    @Column(name = "weapon_name", length = 100)
    private String weaponName;

    @Column(name = "category_id", length = 100)
    private String categoryId;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "rarity_id", length = 50)
    private String rarityId;

    @Column(name = "rarity_name", length = 50)
    private String rarityName;

    @Column(name = "rarity_color", length = 10)
    private String rarityColor;

    @Column(name = "min_float", precision = 10, scale = 8)
    private BigDecimal minFloat;

    @Column(name = "max_float", precision = 10, scale = 8)
    private BigDecimal maxFloat;

    @Builder.Default
    @Column(name = "stattrak", nullable = false)
    private Boolean stattrak = false;

    @Builder.Default
    @Column(name = "souvenir", nullable = false)
    private Boolean souvenir = false;

    @Column(name = "paint_index", length = 20)
    private String paintIndex;

    @Builder.Default
    @Column(name = "legacy_model", nullable = false)
    private Boolean legacyModel = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data", columnDefinition = "jsonb", nullable = false)
    private JsonNode rawData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
