package com.service.sector.aggregator.service;

import com.service.sector.aggregator.data.dto.CreateMasterRequest;
import com.service.sector.aggregator.data.entity.Master;

import java.util.List;
import java.util.Optional;

public interface MasterService {
    List<Master> getAllMasters();
    Optional<Master> getMasterById(Long id);
    Master createMaster(CreateMasterRequest request);
}

