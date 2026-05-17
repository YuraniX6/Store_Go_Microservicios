package com.storego.inventoryservice.dto;

import com.storego.inventoryservice.entity.Rarity;
import com.storego.inventoryservice.entity.Wear;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkinResponse {

    private UUID id;
    private UUID ownerId;
    private String name;
    private String weapon;
    private Rarity rarity;
    private Wear wear;
    private BigDecimal floatValue;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
