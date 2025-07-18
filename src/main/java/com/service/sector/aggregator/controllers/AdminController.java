package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.dto.PendingLandlordDto;
import com.service.sector.aggregator.data.dto.UserStatusChangeRequest;
import com.service.sector.aggregator.data.dto.UserStatusChanged;
import com.service.sector.aggregator.data.enums.RoleRequestStatus;
import com.service.sector.aggregator.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Workspace‑related admin operations.
 */
@Tag(name = "Admin • Workspaces", description = "Operations require admin role")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * GET /api/admin/landlords – list users whose house owner role is waiting approval.
     */
    @Operation(summary = "List landlords")
    @GetMapping("/landlords")
    public ResponseEntity<List<PendingLandlordDto>> listPendingLandlords(
            @RequestParam RoleRequestStatus roleRequestStatus
    ) {
        List<PendingLandlordDto> landlordsToApprove = adminService.getLandlordsByStatus(roleRequestStatus);
        return ResponseEntity.ok(landlordsToApprove);
    }

    @Operation(summary = "Approve landlord")
    @PostMapping("/approve-landlord")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request Approved"),
            @ApiResponse(responseCode = "401", description = "No JWT security token in request"),
            @ApiResponse(responseCode = "403", description = "No admin permissions to approve request"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserStatusChanged> approveLandlordRequest(
            @RequestBody @Valid UserStatusChangeRequest request) {
        UserStatusChanged userStatusChanged = adminService.approveLandlord(request);
        return ResponseEntity.ok(userStatusChanged);
    }

    @Operation(summary = "Reject landlord")
    @PostMapping("/reject-landlord")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request Rejected"),
            @ApiResponse(responseCode = "401", description = "No JWT security token in request"),
            @ApiResponse(responseCode = "403", description = "No admin permissions to reject request"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserStatusChanged> rejectLandlordRequest(
            @RequestBody @Valid UserStatusChangeRequest request) {
        UserStatusChanged userStatusChanged = adminService.rejectLandlord(request);
        return ResponseEntity.ok(userStatusChanged);
    }
}