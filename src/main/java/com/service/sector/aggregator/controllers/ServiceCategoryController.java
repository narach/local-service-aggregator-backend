package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.dto.request.CreateServiceCategoryRequest;
import com.service.sector.aggregator.data.dto.request.UpdateServiceCategoryRequest;
import com.service.sector.aggregator.data.dto.response.ServiceCategoryResponse;
import com.service.sector.aggregator.data.entity.ServiceCategory;
import com.service.sector.aggregator.service.ServiceCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST controller that exposes CRUD endpoints for ServiceCategory.
 */
@RestController
@RequestMapping("/api/admin/service-categories")
@RequiredArgsConstructor
public class ServiceCategoryController {
    private final ServiceCategoryService service;

    @Operation(summary = "List all service categories")
    @GetMapping("/list")
    public List<ServiceCategoryResponse> list() {
        return service.getAll();
    }

    @Operation(
            summary = "Get one service category by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Service category found"),
                    @ApiResponse(responseCode = "404", description = "Service category not found")
            })
    @GetMapping("/{id}")
    public ResponseEntity<ServiceCategoryResponse> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @Operation(summary = "Create a new service category")
    @PostMapping
    public ResponseEntity<ServiceCategory> create(@RequestBody CreateServiceCategoryRequest req) {
        ServiceCategory saved = service.create(req);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @Operation(
            summary = "Update an existing service category",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Service category updated"),
                    @ApiResponse(responseCode = "404", description = "Service category not found")
            })
    @PutMapping("/{id}")
    public ResponseEntity<ServiceCategory> update(@PathVariable Long id,
                                                  @RequestBody UpdateServiceCategoryRequest req) {
        return service.update(id, req)
                .map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @Operation(
            summary = "Delete a service category",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Service category not found")
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean removed = service.delete(id);
        return removed ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
