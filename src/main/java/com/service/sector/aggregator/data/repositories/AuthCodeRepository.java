package com.service.sector.aggregator.data.repositories;

import com.service.sector.aggregator.data.entity.AuthCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthCodeRepository extends JpaRepository<AuthCode, Long> {
    Optional<AuthCode> findByPhone(String phone);

    List<AuthCode> findAllByPhone(String phone);

    void deleteAllByPhone(String phone);
}
