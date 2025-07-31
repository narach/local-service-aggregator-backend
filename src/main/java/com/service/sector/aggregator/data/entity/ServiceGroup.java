package com.service.sector.aggregator.data.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity that maps to the service_group table.
 */
@Entity
@Table(name = "service_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 1024)
    private String description;
}
