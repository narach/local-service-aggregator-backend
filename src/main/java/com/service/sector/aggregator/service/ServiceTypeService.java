package com.service.sector.aggregator.service;

import com.service.sector.aggregator.data.dto.service.ServiceTypeDtos;
import com.service.sector.aggregator.data.entity.ServiceCategory;
import com.service.sector.aggregator.data.entity.ServiceType;
import com.service.sector.aggregator.data.repositories.ServiceCategoryRepository;
import com.service.sector.aggregator.data.repositories.ServiceTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceTypeService {

    private final ServiceTypeRepository typeRepo;
    private final ServiceCategoryRepository categoryRepo;

    /* ---------- Read --------------------------------------------------- */

    public List<ServiceTypeDtos.Response> getAll() {
        return typeRepo.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public Optional<ServiceTypeDtos.Response> getById(Long id) {
        return typeRepo.findById(id).map(this::toDto);
    }

    /* ---------- Create ------------------------------------------------- */

    @Transactional
    public ServiceTypeDtos.Response create(ServiceTypeDtos.CreateOrUpdateRequest req) {
        ServiceCategory category = categoryRepo.findById(req.categoryId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "ServiceCategory %d not found".formatted(req.categoryId())));

        ServiceType entity = ServiceType.builder()
                .name(req.name())
                .description(req.description())
                .category(category)
                .build();

        return toDto(typeRepo.save(entity));
    }

    /* ---------- Update ------------------------------------------------- */

    @Transactional
    public Optional<ServiceTypeDtos.Response> update(Long id,
                                                     ServiceTypeDtos.CreateOrUpdateRequest req) {
        return typeRepo.findById(id).map(entity -> {
            ServiceCategory category = categoryRepo.findById(req.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "ServiceCategory %d not found".formatted(req.categoryId())));

            entity.setName(req.name());
            entity.setDescription(req.description());
            entity.setCategory(category);
            return toDto(entity); // dirty-checked & flushed automatically
        });
    }

    /* ---------- Delete ------------------------------------------------- */

    @Transactional
    public boolean delete(Long id) {
        if (typeRepo.existsById(id)) {
            typeRepo.deleteById(id);
            return true;
        }
        return false;
    }

    /* ---------- Mapping helpers --------------------------------------- */

    private ServiceTypeDtos.Response toDto(ServiceType entity) {
        return new ServiceTypeDtos.Response(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                new ServiceTypeDtos.CategoryBrief(
                        entity.getCategory().getId(),
                        entity.getCategory().getName())
        );
    }
}
