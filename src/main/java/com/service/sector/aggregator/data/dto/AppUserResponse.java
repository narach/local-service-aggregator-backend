package com.service.sector.aggregator.data.dto;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Public view of AppUser returned after registration/login.
 * Password is <i>never</i> exposed.
 */
public record AppUserResponse(
        Long id,
        String phone,
        String realName,
        Set<String> roles,
        OffsetDateTime createdAt
) {}
