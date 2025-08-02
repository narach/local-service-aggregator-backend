package com.service.sector.aggregator.data.repositories;

import com.service.sector.aggregator.data.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    @Query("select c from ServiceCategory c join fetch c.group")
    List<ServiceCategory> findAllWithGroup();

    @Query("select c from ServiceCategory c join fetch c.group where c.id = :id")
    Optional<ServiceCategory> findByIdWithGroup(Long id);
}