package com.storego.inventoryservice.dto;

import com.storego.inventoryservice.entity.Rarity;
import com.storego.inventoryservice.entity.Wear;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSkinRequest {

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 1, max = 150, message = "Name must be between 1 and 150 characters")
    private String name;

    @NotBlank(message = "Weapon cannot be blank")
    @Size(min = 1, max = 50, message = "Weapon must be between 1 and 50 characters")
    private String weapon;

    @NotNull(message = "Rarity cannot be null")
    private Rarity rarity;

    @NotNull(message = "Wear cannot be null")
    private Wear wear;

    @NotNull(message = "FloatValue cannot be null")
    @DecimalMin(value = "0.0", message = "FloatValue must be at least 0.0")
    @DecimalMax(value = "1.0", message = "FloatValue must be at most 1.0")
    private BigDecimal floatValue;

    @Size(max = 500, message = "ImageUrl must not exceed 500 characters")
    @URL(message = "ImageUrl must be a valid URL")
    private String imageUrl;
}
