package com.service.sector.aggregator.data.dto;

import jakarta.validation.constraints.*;

public record AppUserRequest(
        @NotBlank
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone must contain 7-15 digits, optional leading +")
        String phone,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank
        @Pattern(regexp = "^[0-9]{6}$", message = "smsCode must be 6 digits")
        String smsCode
) { }
