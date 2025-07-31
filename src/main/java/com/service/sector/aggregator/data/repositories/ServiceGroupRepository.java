package com.service.sector.aggregator.data.repositories;

import com.service.sector.aggregator.data.entity.ServiceGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Basic CRUD repository for ServiceGroup.
 */
@Repository
public interface ServiceGroupRepository extends JpaRepository<ServiceGroup, Long> {
}
