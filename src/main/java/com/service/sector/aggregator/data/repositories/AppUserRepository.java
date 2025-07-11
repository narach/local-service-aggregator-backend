package com.service.sector.aggregator.data.repositories;

import com.service.sector.aggregator.data.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    boolean existsByPhone(String phone);
    Optional<AppUser> findByPhone(String phone);
}
