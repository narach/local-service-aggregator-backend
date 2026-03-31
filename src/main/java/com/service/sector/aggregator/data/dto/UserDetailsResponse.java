package com.service.sector.aggregator.data.dto;

import java.util.Set;

public record UserDetailsResponse(
        String phone,
        String firstName,
        String lastName,
        Set<String> roles
) {}
