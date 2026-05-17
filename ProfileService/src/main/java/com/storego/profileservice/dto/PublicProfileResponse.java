package com.storego.profileservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Perfil público del usuario (sin información sensible)")
public class PublicProfileResponse {

    @Schema(description = "UUID único del usuario", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez García")
    private String fullname;

    @Schema(description = "Idioma preferido en formato ISO 639-1", example = "es")
    private String language;

    @Schema(description = "Descripción o biografía del usuario", example = "Emprendedor tecnológico")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Schema(description = "Fecha de creación del perfil", example = "2024-05-17T10:30:00Z")
    private Instant createdAt;
}
