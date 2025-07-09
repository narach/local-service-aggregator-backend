package com.service.sector.aggregator.data.repositories;

import com.service.sector.aggregator.data.entity.Role;
import com.service.sector.aggregator.data.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(RoleName name);
}
