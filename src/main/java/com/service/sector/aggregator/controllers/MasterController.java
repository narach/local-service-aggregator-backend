package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.dto.CreateMasterRequest;
import com.service.sector.aggregator.data.entity.Master;
import com.service.sector.aggregator.service.MasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/masters")
@RequiredArgsConstructor
public class MastersController {

    private final MasterService masterService;

    @Operation(summary = "List all masters")
    @GetMapping("/list")
    public List<Master> listMasters() {
        return masterService.getAllMasters();
    }

    @Operation(
            summary = "Get one master by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Master found"),
                    @ApiResponse(responseCode = "404", description = "No master with that ID")
            })
    @GetMapping("/{id}")
    public ResponseEntity<Master> getMasterById(@PathVariable Long id) {
        return masterService.getMasterById(id)
                .map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @Operation(summary = "Create a new master",
               description = "Adds a hairdresser/plumber/etc. and returns the saved entity.")
    @PostMapping
    public ResponseEntity<Master> create(@RequestBody CreateMasterRequest req) {
        Master saved = masterService.createMaster(req);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(saved);
    }
}