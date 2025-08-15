package com.service.sector.aggregator.service;

import com.service.sector.aggregator.data.dto.request.CreateServiceCategoryRequest;
import com.service.sector.aggregator.data.dto.request.UpdateServiceCategoryRequest;
import com.service.sector.aggregator.data.dto.response.ServiceCategoryResponse;
import com.service.sector.aggregator.data.dto.response.ServiceGroupResponse;
import com.service.sector.aggregator.data.entity.ServiceCategory;
import com.service.sector.aggregator.data.entity.ServiceGroup;
import com.service.sector.aggregator.data.repositories.ServiceCategoryRepository;
import com.service.sector.aggregator.data.repositories.ServiceGroupRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Business layer for ServiceCategory.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceCategoryService {

    private final ServiceCategoryRepository categoryRepo;
    private final ServiceGroupRepository groupRepo;

    public List<ServiceCategoryResponse> getAll() {
        return categoryRepo.findAllWithGroup().stream()
                .map(c -> new ServiceCategoryResponse(
                        c.getId(), c.getName(), c.getDescription(), new ServiceGroupResponse(c.getGroup().getId(), c.getGroup().getName(), c.getDescription())))
                .toList();

    }

    public Optional<ServiceCategoryResponse> getById(Long id) {
        return categoryRepo.findByIdWithGroup(id)
                .map(c -> new ServiceCategoryResponse(
                        c.getId(), c.getName(), c.getDescription(), new ServiceGroupResponse(c.getGroup().getId(), c.getGroup().getName(), c.getDescription())));
    }

    @Transactional
    public Optional<ServiceCategoryResponse> create(CreateServiceCategoryRequest req) {
        ServiceGroup group = groupRepo.findById(req.groupId())
                .orElseThrow(() -> new IllegalArgumentException("ServiceGroup not found: " + req.groupId()));

        ServiceCategory categoryEntity = ServiceCategory.builder()
                .name(req.name())
                .description(req.description())
                .group(group)
                .build();

        categoryRepo.save(categoryEntity);
        
       return Optional.of(categoryEntity).map(c -> new ServiceCategoryResponse(
                c.getId(), c.getName(), c.getDescription(), new ServiceGroupResponse(c.getGroup().getId(), c.getGroup().getName(), c.getDescription())));
    }

    @Transactional
    public Optional<ServiceCategoryResponse> update(Long id, UpdateServiceCategoryRequest req) {
        return categoryRepo.findById(id).map(entity -> {
            ServiceGroup group = groupRepo.findById(req.groupId())
                    .orElseThrow(() -> new IllegalArgumentException("ServiceGroup not found: " + req.groupId()));

            entity.setName(req.name());
            entity.setDescription(req.description());
            entity.setGroup(group);

            // JPA dirty checking will persist changes on transaction commit
            return new ServiceCategoryResponse(
                    entity.getId(),
                    entity.getName(),
                    entity.getDescription(),
                    new ServiceGroupResponse(entity.getGroup().getId(), entity.getGroup().getName(), entity.getDescription())
            );
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (categoryRepo.existsById(id)) {
            categoryRepo.deleteById(id);
            return true;
        }
        return false;
    }
}
