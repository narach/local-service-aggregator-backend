package com.service.sector.aggregator.data.dto;

import com.service.sector.aggregator.data.entity.Role;
import com.service.sector.aggregator.data.enums.ActivationStatus;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Public view of AppUser returned after registration/login.
 * Password is <i>never</i> exposed.
 */
public record AppUserResponse(
        Long id,
        String phone,
        ActivationStatus activationStatus,
        java.util.Set<String> roles,
        OffsetDateTime createdAt
) {}
