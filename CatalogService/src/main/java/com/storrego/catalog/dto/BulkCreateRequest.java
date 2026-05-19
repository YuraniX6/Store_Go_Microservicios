package com.storrego.catalog.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkCreateRequest {

    @NotNull
    @NotEmpty
    private List<JsonNode> skins;
}
