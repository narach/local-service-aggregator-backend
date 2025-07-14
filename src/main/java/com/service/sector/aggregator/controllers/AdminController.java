package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.dto.AppUserRequest;
import com.service.sector.aggregator.data.dto.AppUserResponse;
import com.service.sector.aggregator.data.dto.ApproveStatusChangeRequest;
import com.service.sector.aggregator.data.dto.UserStatusChanged;
import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.enums.RoleName;
import com.service.sector.aggregator.data.enums.RoleRequestStatus;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Workspace‑related admin operations.
 */
@Tag(name = "Admin • Workspaces", description = "Operations require admin role")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AppUserRepository userRepo;

    /**
     * GET /api/admin/landlords – list users whose house owner role is waiting approval.
     */
    @Operation(summary = "List landlords")
    @GetMapping("/landlords")
    public ResponseEntity<List<AppUser>> listPendingLandlords(
            @RequestParam RoleRequestStatus roleRequestStatus
    ) {
        List<AppUser> pending = userRepo.findByLandlordRoleStatus(roleRequestStatus);
        return ResponseEntity.ok(pending);
    }

    @Operation(summary = "Approve landlord")
    @PostMapping("/approve-landlord")
    @Transactional
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request Approved"),
            @ApiResponse(responseCode = "401", description = "No JWT security token in request"),
            @ApiResponse(responseCode = "403", description = "No admin permissions to approve request"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserStatusChanged> approveLandlordRequest(
            @RequestBody @Valid ApproveStatusChangeRequest request) {
        AppUser user = userRepo.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        RoleRequestStatus oldStatus = user.getLandlordRoleStatus();
        RoleRequestStatus newStatus = RoleRequestStatus.APPROVED;
        user.setLandlordRoleStatus(newStatus);
        userRepo.save(user);

        return ResponseEntity.ok(new UserStatusChanged(user.getId(), RoleName.LANDLORD, oldStatus, newStatus));
    }

}
