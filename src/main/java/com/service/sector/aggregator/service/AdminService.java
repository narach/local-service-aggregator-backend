package com.service.sector.aggregator.service;

import com.service.sector.aggregator.data.dto.PendingLandlordDto;
import com.service.sector.aggregator.data.dto.UserStatusChangeRequest;
import com.service.sector.aggregator.data.dto.UserStatusChanged;
import com.service.sector.aggregator.data.enums.RoleRequestStatus;

import java.util.List;

public interface AdminService {
    List<PendingLandlordDto> getLandlordsByStatus(RoleRequestStatus status);
    UserStatusChanged approveLandlord(UserStatusChangeRequest request);
    UserStatusChanged rejectLandlord(UserStatusChangeRequest request);
}

