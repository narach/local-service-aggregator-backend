package com.service.sector.aggregator.data.repositories;

import com.service.sector.aggregator.data.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    // Additional query methods can be declared here when needed
}
