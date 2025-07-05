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

    /**
     * Арендодатель (предполагаем, что AppUser-сущность уже есть)
     */
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

    /**
     * Тип/категория (пока строка; можно заменить Enum/справочник)
     */
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

    /**
     * Юр. лицо / ИП (опционально)
     */
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

    /**
     * Фото (cascade – по ситуации: PERSIST, MERGE, REMOVE)
     */
    @OneToMany(mappedBy = "workspace", orphanRemoval = true, cascade = CascadeType.ALL)
    @OrderBy("order ASC")
    private List<WorkspacePhoto> photos = new ArrayList<>();

    public Workspace() {}

    public Workspace(Long id, AppUser owner, String name, String city, String address, Double latitude, Double longitude, String kind, String description, LocalTime openTime, LocalTime closeTime, short workingDaysMask, int minRentMinutes, BigDecimal pricePerHour, String legalName, String legalRegistrationNo, String legalDetails, boolean termsAccepted, OffsetDateTime termsAcceptedAt, Status status, OffsetDateTime createdAt, OffsetDateTime updatedAt, List<WorkspacePhoto> photos) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.city = city;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.kind = kind;
        this.description = description;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.workingDaysMask = workingDaysMask;
        this.minRentMinutes = minRentMinutes;
        this.pricePerHour = pricePerHour;
        this.legalName = legalName;
        this.legalRegistrationNo = legalRegistrationNo;
        this.legalDetails = legalDetails;
        this.termsAccepted = termsAccepted;
        this.termsAcceptedAt = termsAcceptedAt;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.photos = photos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public short getWorkingDaysMask() {
        return workingDaysMask;
    }

    public void setWorkingDaysMask(short workingDaysMask) {
        this.workingDaysMask = workingDaysMask;
    }

    public int getMinRentMinutes() {
        return minRentMinutes;
    }

    public void setMinRentMinutes(int minRentMinutes) {
        this.minRentMinutes = minRentMinutes;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getLegalRegistrationNo() {
        return legalRegistrationNo;
    }

    public void setLegalRegistrationNo(String legalRegistrationNo) {
        this.legalRegistrationNo = legalRegistrationNo;
    }

    public String getLegalDetails() {
        return legalDetails;
    }

    public void setLegalDetails(String legalDetails) {
        this.legalDetails = legalDetails;
    }

    public boolean isTermsAccepted() {
        return termsAccepted;
    }

    public void setTermsAccepted(boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }

    public OffsetDateTime getTermsAcceptedAt() {
        return termsAcceptedAt;
    }

    public void setTermsAcceptedAt(OffsetDateTime termsAcceptedAt) {
        this.termsAcceptedAt = termsAcceptedAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<WorkspacePhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<WorkspacePhoto> photos) {
        this.photos = photos;
    }

    /* --- enum & boilerplate --- */

    public enum Status {DRAFT, UNDER_REVIEW, APPROVED, REJECTED}

    /* getters / setters / equals / hashCode / toString */
}

