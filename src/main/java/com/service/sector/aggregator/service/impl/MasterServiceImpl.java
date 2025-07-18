package com.service.sector.aggregator.service;

import com.service.sector.aggregator.data.dto.CreateMasterRequest;
import com.service.sector.aggregator.data.entity.Master;
import com.service.sector.aggregator.data.repositories.MasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MasterServiceImpl implements MasterService {

    private final MasterRepository repo;

    @Override
    public List<Master> getAllMasters() {
        return repo.findAll();
    }

    @Override
    public Optional<Master> getMasterById(Long id) {
        return repo.findById(id);
    }

    @Override
    public Master createMaster(CreateMasterRequest request) {
        return repo.save(new Master(null, request.name(), request.speciality()));
    }
}