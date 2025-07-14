package com.service.sector.aggregator.data.dto;

import java.util.List;

public record WorkspaceSummaryDto(Long id,
                                  String name,
                                  String city,
                                  String address,
                                  List<WorkspacePhotoDto> photos) {}

