package com.storrego.catalog.controller;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import com.storrego.catalog.dto.BulkCreateRequest;
import com.storrego.catalog.dto.BulkCreateResponse;
import com.storrego.catalog.dto.CatalogSkinDetailResponse;
import com.storrego.catalog.dto.CatalogSkinSummaryResponse;
import com.storrego.catalog.dto.PagedResponse;
import com.storrego.catalog.service.CatalogSkinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalog/skins")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "CS:GO skin catalog API")
public class CatalogSkinController {

    private final CatalogSkinService service;

    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "List skins with pagination and optional filters")
    @ApiResponse(responseCode = "200", description = "Paginated skin list")
    public ResponseEntity<PagedResponse<CatalogSkinSummaryResponse>> list(
            @RequestParam(required = false) String weapon,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) Boolean stattrak,
            @RequestParam(required = false) Boolean souvenir,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(service.list(weapon, category, rarity, stattrak, souvenir, q, pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get all skins without pagination")
    @ApiResponse(responseCode = "200", description = "Full skin list ordered by name")
    public ResponseEntity<List<CatalogSkinSummaryResponse>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get skin detail by ID")
    @ApiResponse(responseCode = "200", description = "Skin detail including rawData")
    @ApiResponse(responseCode = "404", description = "Skin not found")
    public ResponseEntity<CatalogSkinDetailResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new skin (ADMIN only)")
    @ApiResponse(responseCode = "201", description = "Skin created")
    @ApiResponse(responseCode = "409", description = "Skin ID already exists")
    public ResponseEntity<CatalogSkinDetailResponse> create(@RequestBody JsonNode body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk create skins (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Bulk result with per-skin success/error")
    public ResponseEntity<BulkCreateResponse> bulkCreate(@RequestBody @Valid BulkCreateRequest request) {
        return ResponseEntity.ok(service.bulkCreate(request.getSkins()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a skin (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Skin updated")
    @ApiResponse(responseCode = "404", description = "Skin not found")
    public ResponseEntity<CatalogSkinDetailResponse> update(
            @PathVariable String id,
            @RequestBody JsonNode body) {
        return ResponseEntity.ok(service.update(id, body));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a skin (ADMIN only)")
    @ApiResponse(responseCode = "204", description = "Skin deleted")
    @ApiResponse(responseCode = "404", description = "Skin not found")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
