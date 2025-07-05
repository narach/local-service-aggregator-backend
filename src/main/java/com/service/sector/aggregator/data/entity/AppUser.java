package com.service.sector.aggregator.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "app_user",
        uniqueConstraints = @UniqueConstraint(name = "uc_user_email", columnNames = "email"))
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email @NotBlank
    @Column(nullable = false, length = 255)
    private String email;

    @NotBlank
    @Column(name = "real_name", nullable = false, length = 255)
    private String realName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    /* getters / setters / equals / hashCode / toString */
}

