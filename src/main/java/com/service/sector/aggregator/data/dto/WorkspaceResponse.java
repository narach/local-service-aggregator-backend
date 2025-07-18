package com.service.sector.aggregator.data.dto;

import java.util.List;

public record WorkspaceResponse(Long id, String name, String city, List<String> photoUrls) {}