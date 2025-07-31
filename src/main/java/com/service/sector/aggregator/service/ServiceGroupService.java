package com.service.sector.aggregator.service;

import com.service.sector.aggregator.data.dto.request.CreateServiceGroupRequest;
import com.service.sector.aggregator.data.dto.request.UpdateServiceGroupRequest;
import com.service.sector.aggregator.data.entity.ServiceGroup;
import com.service.sector.aggregator.data.repositories.ServiceGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Business layer for ServiceGroup.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceGroupService {

    private final ServiceGroupRepository repository;

    /**
     * findAll – returns every service group.
     */
    public List<ServiceGroup> getAllGroups() {
        return repository.findAll();
    }

    /**
     * findById – returns optional group by primary key.
     */
    public Optional<ServiceGroup> getGroupById(Long id) {
        return repository.findById(id);
    }

    /**
     * addNew – persists a new service group.
     */
    @Transactional
    public ServiceGroup createGroup(CreateServiceGroupRequest req) {
        ServiceGroup entity = ServiceGroup.builder()
                .name(req.name())
                .description(req.description())
                .build();
        return repository.save(entity);
    }

    /**
     * update – changes name/description if the entity exists.
     *
     * @return updated entity or {@code Optional.empty()} if not found
     */
    @Transactional
    public Optional<ServiceGroup> updateGroup(Long id, UpdateServiceGroupRequest req) {
        return repository.findById(id).map(entity -> {
            entity.setName(req.name());
            entity.setDescription(req.description());
            return entity;                 // saved automatically by JPA dirty checking
        });
    }

    /**
     * delete – removes the entity by primary key.
     *
     * @return {@code true} if deleted, {@code false} if not found
     */
    @Transactional
    public boolean deleteGroup(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

}
