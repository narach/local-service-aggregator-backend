package com.service.sector.aggregator.data.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalTime;
import java.util.List;

@Schema(name = "WorkspaceForm")
public record WorkspaceForm(
        @NotBlank String name,
        @NotBlank String city,
        @NotBlank String address,
        @NotBlank String kind,
        String description,
        @NotNull LocalTime openTime,
        @NotNull LocalTime closeTime,

        @Size(min = 1, max = 7) List<String> workingDays,

        @Positive int minRentMinutes,
        @DecimalMin("0.01") java.math.BigDecimal pricePerHour,
        /* optional legal */
        String legalName,
        String legalRegistrationNo,
        String legalDetails
) {}
