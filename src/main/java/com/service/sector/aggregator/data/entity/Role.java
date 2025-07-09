package com.service.sector.aggregator.data.entity;

import com.service.sector.aggregator.data.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role",
        uniqueConstraints = @UniqueConstraint(name = "uc_role_name", columnNames = "role_name"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false, length = 50)
    private RoleName roleName;

    private String description;
}
