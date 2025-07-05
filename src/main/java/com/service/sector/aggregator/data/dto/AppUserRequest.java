package com.service.sector.aggregator.data.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AppUserRequest(
        @Email @NotBlank String email,
        @NotBlank String realName
) {}
