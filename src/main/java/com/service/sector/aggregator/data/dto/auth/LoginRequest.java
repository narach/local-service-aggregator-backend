package com.service.sector.aggregator.data.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$") String phone
) {}
