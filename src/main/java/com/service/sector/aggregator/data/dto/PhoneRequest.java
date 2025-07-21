package com.service.sector.aggregator.data.dto;

import jakarta.validation.constraints.NotBlank;

public record PhoneRequest(@NotBlank String phone) {
}
