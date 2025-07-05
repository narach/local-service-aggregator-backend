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

    public WorkspacePhoto() {}

    public WorkspacePhoto(Long id, Workspace workspace, String filePath, short order, OffsetDateTime createdAt) {
        this.id = id;
        this.workspace = workspace;
        this.filePath = filePath;
        this.order = order;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public short getOrder() {
        return order;
    }

    public void setOrder(short order) {
        this.order = order;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /* getters / setters / equals / hashCode */
}

