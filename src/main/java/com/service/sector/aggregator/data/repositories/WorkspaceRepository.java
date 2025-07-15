package com.service.sector.aggregator.data.repositories;

import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    List<Workspace> findAllByOwner(AppUser owner);

    List<Workspace> findAllByOwnerId(Long ownerId);
}
