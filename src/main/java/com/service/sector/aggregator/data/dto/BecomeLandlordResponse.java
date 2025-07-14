package com.service.sector.aggregator.data.dto;

import com.service.sector.aggregator.data.enums.RoleRequestStatus;

public record BecomeLandlordResponse(Long userId, String userName, RoleRequestStatus landlordStatus,
                                     WorkspaceResponse workspaceResponse) {
}
