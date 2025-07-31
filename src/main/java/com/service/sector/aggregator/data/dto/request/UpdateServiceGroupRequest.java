package com.service.sector.aggregator.data.dto.request;

/**
 * DTO used when updating an existing ServiceGroup.
 *
 * @param name        new display name (required)
 * @param description new description (nullable)
 */
public record UpdateServiceGroupRequest(String name, String description) {
}

