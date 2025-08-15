package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.dto.service.ServiceTypeDtos;
import com.service.sector.aggregator.service.ServiceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin/service-types")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final ServiceTypeService service;

    /* ---------- Read --------------------------------------------------- */

    @GetMapping
    public List<ServiceTypeDtos.Response> findAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceTypeDtos.Response> findById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /* ---------- Create ------------------------------------------------- */

    @PostMapping
    public ResponseEntity<ServiceTypeDtos.Response> create(
            @RequestBody ServiceTypeDtos.CreateOrUpdateRequest req) {

        ServiceTypeDtos.Response dto = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/service-types/" + dto.id()))
                .body(dto);
    }

    /* ---------- Update ------------------------------------------------- */

    @PutMapping("/{id}")
    public ResponseEntity<ServiceTypeDtos.Response> update(
            @PathVariable Long id,
            @RequestBody ServiceTypeDtos.CreateOrUpdateRequest req) {

        return service.update(id, req)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /* ---------- Delete ------------------------------------------------- */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
