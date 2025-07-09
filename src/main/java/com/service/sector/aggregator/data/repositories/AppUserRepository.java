package com.service.sector.aggregator.data.repositories;

import com.service.sector.aggregator.data.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data интерфейс для доступа к таблице app_user.
 *<p>
 * Методы CRUD предоставляются JpaRepository.
 */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByPhone(String phone);
    Optional<AppUser> findByEmailOrPhone(String email, String phone);
}
