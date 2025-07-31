package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.dto.request.CreateServiceGroupRequest;
import com.service.sector.aggregator.data.dto.request.UpdateServiceGroupRequest;
import com.service.sector.aggregator.data.entity.ServiceGroup;
import com.service.sector.aggregator.service.ServiceGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST controller that exposes the required endpoints for ServiceGroup.
 */
@RestController
@RequestMapping("/api/admin/service-groups")
@RequiredArgsConstructor
public class ServiceGroupController {

    private final ServiceGroupService service;

    @Operation(summary = "List all service groups")
    @GetMapping("/list")
    public List<ServiceGroup> listGroups() {
        return service.getAllGroups();
    }

    @Operation(
            summary = "Get one service group by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Service group found"),
                    @ApiResponse(responseCode = "404", description = "No service group with that ID")
            })
    @GetMapping("/{id}")
    public ResponseEntity<ServiceGroup> getGroupById(@PathVariable Long id) {
        return service.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @Operation(summary = "Create a new service group")
    @PostMapping
    public ResponseEntity<ServiceGroup> create(@RequestBody CreateServiceGroupRequest req) {
        ServiceGroup saved = service.createGroup(req);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
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
    public ResponseEntity<ServiceGroup> update(@PathVariable Long id,
                                               @RequestBody UpdateServiceGroupRequest req) {
        return service.updateGroup(id, req)
                .map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @Operation(
            summary = "Delete a service group",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Service group not found")
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean removed = service.deleteGroup(id);
        return removed ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

}
