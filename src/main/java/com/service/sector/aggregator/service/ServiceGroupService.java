package com.service.sector.aggregator.service;

import com.service.sector.aggregator.data.dto.request.CreateServiceGroupRequest;
import com.service.sector.aggregator.data.dto.request.UpdateServiceGroupRequest;
import com.service.sector.aggregator.data.dto.response.ServiceGroupResponse;
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
    public List<ServiceGroupResponse> getAllGroups() {
        return repository.findAll().stream()
                .map(g -> new ServiceGroupResponse(g.getId(), g.getName(), g.getDescription()))
                .toList();
    }

    /**
     * findById – returns optional group by primary key.
     */
    public Optional<ServiceGroupResponse> getGroupById(Long id) {
        return repository.findById(id)
                .map(g -> new ServiceGroupResponse(g.getId(), g.getName(), g.getDescription()));
    }

    /**
     * addNew – persists a new service group.
     */
    @Transactional
    public ServiceGroupResponse createGroup(CreateServiceGroupRequest req) {
        ServiceGroup entity = ServiceGroup.builder()
                .name(req.name())
                .description(req.description())
                .build();
        ServiceGroup saved = repository.save(entity);
        return new ServiceGroupResponse(saved.getId(), saved.getName(), saved.getDescription());
    }

    /**
     * update – changes name/description if the entity exists.
     *
     * @return updated DTO or {@code Optional.empty()} if not found
     */
    @Transactional
    public Optional<ServiceGroupResponse> updateGroup(Long id, UpdateServiceGroupRequest req) {
        return repository.findById(id).map(entity -> {
            entity.setName(req.name());
            entity.setDescription(req.description());
            return new ServiceGroupResponse(entity.getId(), entity.getName(), entity.getDescription());
        });
    }

    /**
     * delete – removes group by id.
     *
     * @return true if a row was deleted
     */
    @Transactional
    public boolean deleteGroup(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}