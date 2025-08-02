package com.service.sector.aggregator.data.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity mapped to the service_category table.
 * Many categories can belong to one service group.
 */
@Entity
@Table(name = "service_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(length = 300)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private ServiceGroup group;
}
