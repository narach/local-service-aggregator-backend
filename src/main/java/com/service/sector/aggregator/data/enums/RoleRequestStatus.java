package com.service.sector.aggregator.data.enums;

public enum RoleRequestStatus {
    NO,               // user never asked for this role
    WAITING_APPROVAL, // asked, admin review pending
    APPROVED,         // role granted
    REJECTED          // request was denied
}
