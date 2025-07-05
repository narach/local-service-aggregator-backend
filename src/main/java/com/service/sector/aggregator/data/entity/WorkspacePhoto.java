package com.service.sector.aggregator.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "workspace_photo",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_photo_order",
                        columnNames = { "workspace_id", "\"order\"" })
        })
public class WorkspacePhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @NotBlank
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /** Порядок отображения (0…14) */
    @Min(0) @Max(14)
    @Column(name = "\"order\"", nullable = false)
    private short order;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /* getters / setters / equals / hashCode */
}

