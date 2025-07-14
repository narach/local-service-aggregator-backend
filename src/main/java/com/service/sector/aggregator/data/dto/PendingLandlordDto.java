package com.service.sector.aggregator.data.dto;

import java.util.List;

public record PendingLandlordDto(long userId, String phone, String realName, List<WorkspaceSummaryDto> workspaces) {
}
