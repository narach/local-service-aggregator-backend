package com.service.sector.aggregator.data.dto;

import java.time.OffsetDateTime;

/**
 * Public view of AppUser returned after registration/login.
 * Password is <i>never</i> exposed.
 */
public record AppUserResponse(
        Long id,
        String email,
        String phone,
        String realName,
        OffsetDateTime createdAt
) {}
