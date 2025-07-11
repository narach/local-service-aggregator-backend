package com.service.sector.aggregator.data.dto;

import jakarta.validation.constraints.NotBlank;

public record ActivationRequest(
        @NotBlank String phone,
        @NotBlank String code
) {}
