package com.service.sector.aggregator.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalTime;
import java.util.*;

@Entity
@Table(name = "workspace")
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Арендодатель (предполагаем, что AppUser-сущность уже есть) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @NotBlank
    private String name;

    @NotBlank
    private String city;

    @NotBlank
    private String address;

    private Double latitude;
    private Double longitude;

    /** Тип/категория (пока строка; можно заменить Enum/справочник) */
    @NotBlank
    private String kind;

    private String description;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    /**
     * Бит-маска 7 дней (Mon=1, Sun=64).
     * 0b0111110 = 62 → Пн-Пт рабочие, Сб-Вс выходные.
     */
    @Column(name = "working_days_mask", nullable = false)
    private short workingDaysMask = 62;

    @Min(1)
    @Column(name = "min_rent_minutes", nullable = false)
    private int minRentMinutes;

    @DecimalMin("0.01")
    @Column(name = "price_per_hour", precision = 10, scale = 2, nullable = false)
    private BigDecimal pricePerHour;

    /** Юр. лицо / ИП (опционально) */
    private String legalName;
    private String legalRegistrationNo;
    private String legalDetails;

    @Column(nullable = false)
    private boolean termsAccepted = false;

    private OffsetDateTime termsAcceptedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    /** Фото (cascade – по ситуации: PERSIST, MERGE, REMOVE) */
    @OneToMany(mappedBy = "workspace", orphanRemoval = true, cascade = CascadeType.ALL)
    @OrderBy("order ASC")
    private List<WorkspacePhoto> photos = new ArrayList<>();

    /* --- enum & boilerplate --- */

    public enum Status { DRAFT, UNDER_REVIEW, APPROVED, REJECTED }

    /* getters / setters / equals / hashCode / toString */
}

