package com.service.sector.aggregator.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "filePath"})
@Entity
@Table(name = "workspace_photo",
        uniqueConstraints = @UniqueConstraint(name = "uc_photo_order",
                columnNames = {"workspace_id", "\"order\""}))
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

    /** Display order (0…14) */
    @Min(0)
    @Max(14)
    @Column(name = "\"order\"", nullable = false)
    private short order;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
