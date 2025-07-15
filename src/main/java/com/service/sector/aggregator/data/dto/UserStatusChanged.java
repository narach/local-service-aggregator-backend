package com.service.sector.aggregator.data.dto;

import com.service.sector.aggregator.data.enums.RoleName;
import com.service.sector.aggregator.data.enums.RoleRequestStatus;

public record UserStatusChanged(Long userId, RoleName role, RoleRequestStatus oldStatus, RoleRequestStatus newStatus) {
}
