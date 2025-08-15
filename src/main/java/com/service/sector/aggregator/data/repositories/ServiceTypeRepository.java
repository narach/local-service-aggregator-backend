package com.service.sector.aggregator.data.repositories;

import com.service.sector.aggregator.data.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {
}
