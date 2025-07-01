package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.Master;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController()
@RequestMapping("/masters")
public class MastersController {

    @GetMapping("/list")
    private List<Master> listMasters() {
        List<Master> masters = new ArrayList<>();

        Master m1 = new Master(1L, "Ivanov", "Hairdresser");
        Master m2 = new Master(2L, "Petrov", "Manicure");
        Master m3 = new Master(3L, "Sidorov", "Phone Repair");
        masters.add(m1);
        masters.add(m2);
        masters.add(m3);

        return masters;
    }
}
