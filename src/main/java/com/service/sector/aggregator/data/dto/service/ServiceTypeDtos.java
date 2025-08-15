package com.service.sector.aggregator.data.dto.service;

/**
 * DTOs for ServiceType.
 * Records are used to minimise boiler-plate (Java 17+).
 */
public final class ServiceTypeDtos {

    /** Request used for create / update. */
    public record CreateOrUpdateRequest(Long categoryId,
                                        String name,
                                        String description) { }

    /** Brief view of ServiceCategory inside the response. */
    public record CategoryBrief(Long id,
                                String name) { }

    /** Response sent to the client. */
    public record Response(Long id,
                           String name,
                           String description,
                           CategoryBrief category) { }

    private ServiceTypeDtos() { }
}
