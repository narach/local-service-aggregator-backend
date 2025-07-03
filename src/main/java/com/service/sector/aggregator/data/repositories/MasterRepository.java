package com.service.sector.aggregator.data.repositories;

import com.service.sector.aggregator.data.Master;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MasterRepository extends JpaRepository<Master, Long> {
}
