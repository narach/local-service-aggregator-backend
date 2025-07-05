package com.service.sector.aggregator.data.dto;

import java.time.OffsetDateTime;

public record AppUserResponse(
        Long id,
        String email,
        String realName,
        OffsetDateTime createdAt
) {}
