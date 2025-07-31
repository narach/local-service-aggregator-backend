package com.service.sector.aggregator.data.dto.request;

/**
 * DTO used when creating a new ServiceGroup.
 *
 * @param name        display name (required)
 * @param description optional text description
 */
public record CreateServiceGroupRequest(String name, String description) {
}

