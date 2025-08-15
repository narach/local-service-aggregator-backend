package com.service.sector.aggregator.data.dto.response;

/**
 * DTO sent to the client – contains only scalar values.
 *
 * @param id          category id
 * @param name        category name
 * @param description category description
 * @param group       owning ServiceGroup
 */
public record ServiceCategoryResponse(Long id, String name, String description, ServiceGroupResponse group) {
}
