package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.dto.request.CreateServiceGroupRequest;
import com.service.sector.aggregator.data.dto.request.UpdateServiceGroupRequest;
import com.service.sector.aggregator.data.dto.response.ServiceGroupResponse;
import com.service.sector.aggregator.service.ServiceGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin/service-groups")
@RequiredArgsConstructor
public class ServiceGroupController {

    private final ServiceGroupService service;

    @Operation(summary = "List all service groups")
    @GetMapping
    public List<ServiceGroupResponse> list() {
        return service.getAllGroups();
    }

    @Operation(
            summary = "Get one service group by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Service group found"),
                    @ApiResponse(responseCode = "404", description = "Service group not found")
            })
    @GetMapping("/{id}")
    public ResponseEntity<ServiceGroupResponse> getById(@PathVariable Long id) {
        return service.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @Operation(summary = "Create a new service group")
    @PostMapping
    public ResponseEntity<ServiceGroupResponse> create(@Valid @RequestBody CreateServiceGroupRequest req) {
        ServiceGroupResponse saved = service.createGroup(req);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.id())
                .toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @Operation(
            summary = "Update an existing service group",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Service group updated"),
                    @ApiResponse(responseCode = "404", description = "Service group not found")
            })
    @PutMapping("/{id}")
    public ResponseEntity<ServiceGroupResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateServiceGroupRequest req) {
        return service.updateGroup(id, req)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Delete a service group",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Service group deleted"),
                    @ApiResponse(responseCode = "404", description = "Service group not found")
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean removed = service.deleteGroup(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}