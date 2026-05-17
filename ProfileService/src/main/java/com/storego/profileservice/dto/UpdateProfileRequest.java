package com.storego.profileservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body para actualizar el perfil del usuario. Todos los campos son opcionales.")
public class UpdateProfileRequest {

    @Size(min = 1, max = 150, message = "fullname must be between 1 and 150 characters")
    @Schema(description = "Nombre completo del usuario (opcional)", example = "Juan Pérez García", minLength = 1, maxLength = 150)
    private String fullname;

    @Pattern(regexp = "^[a-z]{2}$", message = "language must be ISO 639-1 format (e.g., es, en, pt)")
    @Schema(description = "Idioma preferido del usuario en formato ISO 639-1 (opcional)", example = "es", pattern = "^[a-z]{2}$")
    private String language;

    @Size(max = 500, message = "description must not exceed 500 characters")
    @Schema(description = "Descripción o biografía del usuario (opcional)", example = "Emprendedor tecnológico", maxLength = 500)
    private String description;
}
