package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.Master;
import com.service.sector.aggregator.data.dto.CreateMasterRequest;
import com.service.sector.aggregator.data.repositories.MasterRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController()
@RequestMapping("/masters")
public class MastersController {

    private final MasterRepository repo;

    public MastersController(MasterRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/list")
    private List<Master> listMasters() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    private ResponseEntity<Master> getMasterById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet( () -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Master> create(@RequestBody CreateMasterRequest req) {
        Master saved = repo.save(new Master(null, req.name(), req.speciality()));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(saved);
    }
}
