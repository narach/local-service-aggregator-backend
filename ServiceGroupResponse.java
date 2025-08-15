package com.service.sector.aggregator.data.dto.response;

/**
 * DTO sent to the client – contains only scalar values.
 *
 * @param id          group id
 * @param name        group name
 * @param description group description
 */
public record ServiceGroupResponse(Long id, String name, String description) {
}
