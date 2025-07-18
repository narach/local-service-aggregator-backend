package com.service.sector.aggregator.data.entity;

import com.service.sector.aggregator.data.enums.WorkspaceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a rentable workspace / room.
 */
@Getter
@Setter
@NoArgsConstructor // JPA‑spec compliant
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name", "city", "kind"})
@Entity
@Table(name = "workspace")
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Landlord (AppUser).
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

    /** Category / kind, e.g., hair chair, massage room. */
    @NotBlank
    private String kind;

    private String description;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    /**
     * Bit‑mask of working days (Mon=1, …, Sun=64). Default 62 = Mon‑Fri.
     */
    @Column(name = "working_days_mask", nullable = false)
    private short workingDaysMask = 62;

    @Min(1)
    @Column(name = "min_rent_minutes", nullable = false)
    private int minRentMinutes;

    @DecimalMin("0.01")
    @Column(name = "price_per_hour", precision = 10, scale = 2, nullable = false)
    private BigDecimal pricePerHour;

    // --- legal details (optional) ---
    private String legalName;
    private String legalRegistrationNo;
    private String legalDetails;

    @Column(nullable = false)
    private boolean termsAccepted = false;

    private OffsetDateTime termsAcceptedAt;

    @Enumerated(EnumType.STRING)          // still stores enum name as text
    @Column(length = 50)                 // optional, matches Liquibase change
    private WorkspaceStatus status = WorkspaceStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "workspace",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("order ASC")
    @Builder.Default                 // <- Lombok initializes field when using @Builder
    private List<WorkspacePhoto> photos = new ArrayList<>();
}
