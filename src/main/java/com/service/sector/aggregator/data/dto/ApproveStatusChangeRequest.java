package com.service.sector.aggregator.data.dto;

import jakarta.validation.constraints.NotNull;

public record ApproveStatusChangeRequest(
        @NotNull Long userId) {
}
