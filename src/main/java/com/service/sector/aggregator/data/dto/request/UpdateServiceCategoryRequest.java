package com.service.sector.aggregator.data.dto.request;

/**
 * DTO used when updating an existing ServiceCategory.
 *
 * @param name        new name (required)
 * @param description new description (nullable)
 * @param groupId     new ServiceGroup id (required)
 */
public record UpdateServiceCategoryRequest(String name, String description, Long groupId) {
}

