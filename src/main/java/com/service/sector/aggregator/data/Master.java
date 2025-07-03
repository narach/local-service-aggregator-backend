package com.service.sector.aggregator.data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Schema(description = "Person who provides a service (hairdresser, etc.)")
@Entity
@Table(name = "masters")
public class Master {

    @Schema(description = "Database ID", example = "42")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Schema(description = "Specialists first and last name or contact name", example = "Ivanov Ivan")
    private String name;
    @Schema(description = "Master's speciality", example = "Hairdresser")
    private String speciality;

    Master() {}

    public Master(Long id, String name, String speciality) {
        this.id = id;
        this.name = name;
        this.speciality = speciality;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }
}
