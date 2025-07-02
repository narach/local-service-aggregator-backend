package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.Master;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController()
@RequestMapping("/masters")
public class MastersController {

    @GetMapping("/list")
    private List<Master> listMasters() {
        return buildSampleMasters();
    }

    @GetMapping("/{ID}")
    private ResponseEntity<Master> getMasterById(@PathVariable Long ID) {
        return buildSampleMasters().stream()
                .filter(m -> m.getId().equals(ID))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound()
                        .build());
    }

    /** helper builds the same dummy data you already use */
    private List<Master> buildSampleMasters() {
        List<Master> masters = new ArrayList<>();
        masters.add(new Master(1L, "Ivanov",  "Hairdresser"));
        masters.add(new Master(2L, "Petrov",  "Manicure"));
        masters.add(new Master(3L, "Sidorov", "Phone Repair"));
        return masters;
    }
}
