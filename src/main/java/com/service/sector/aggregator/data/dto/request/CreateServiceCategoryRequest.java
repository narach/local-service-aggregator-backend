package com.service.sector.aggregator.data.dto.request;

/**
 * DTO used when creating a new ServiceCategory.
 *
 * @param name        category name (required)
 * @param description category description (nullable)
 * @param groupId     owning ServiceGroup id (required)
 */
public record CreateServiceCategoryRequest(String name, String description, Long groupId) {
}

