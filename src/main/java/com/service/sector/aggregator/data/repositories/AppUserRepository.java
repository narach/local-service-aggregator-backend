package com.service.sector.aggregator.data.repositories;

import com.service.sector.aggregator.data.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data интерфейс для доступа к таблице app_user.
 *<p>
 * Методы CRUD предоставляются JpaRepository.
 */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Проверка, зарегистрирован ли уже пользователь с таким email.
     * Spring Data построит реализацию автоматически.
     *
     * @param email e-mail адрес
     * @return true, если запись существует
     */
    boolean existsByEmail(String email);
}
