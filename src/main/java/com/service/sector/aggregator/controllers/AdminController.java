package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.dto.*;
import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.entity.Workspace;
import com.service.sector.aggregator.data.enums.RoleName;
import com.service.sector.aggregator.data.enums.RoleRequestStatus;
import com.service.sector.aggregator.data.enums.WorkspaceStatus;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import com.service.sector.aggregator.data.repositories.WorkspaceRepository;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.ArrayList;
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
    private final WorkspaceRepository workspaceRepo;

    /**
     * GET /api/admin/landlords – list users whose house owner role is waiting approval.
     */
    @Operation(summary = "List landlords")
    @GetMapping("/landlords")
    public ResponseEntity<List<PendingLandlordDto>> listPendingLandlords(
            @RequestParam RoleRequestStatus roleRequestStatus
    ) {
        List<AppUser> users = userRepo.findByLandlordRoleStatus(roleRequestStatus);
        List<PendingLandlordDto> landlordsToApprove = new ArrayList<>();
        users.forEach(user -> {
            landlordsToApprove.add(mapLandlord(user));
        });
        return ResponseEntity.ok(landlordsToApprove);
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

        // Approve user's workspace(s)
        List<Workspace> userWorkspaces = workspaceRepo.findAllByOwner(user);
        userWorkspaces.forEach( workspace -> workspace.setStatus(WorkspaceStatus.APPROVED));
        workspaceRepo.saveAll(userWorkspaces);

        return ResponseEntity.ok(new UserStatusChanged(user.getId(), RoleName.LANDLORD, oldStatus, newStatus));
    }

    PendingLandlordDto mapLandlord(AppUser user) {
        List<WorkspaceSummaryDto> workspacesSummary = workspaceRepo.findAllByOwner(user).stream().map(this::toSummaryDto).toList();
        return new PendingLandlordDto(user.getId(), user.getPhone(), user.getRealName(), workspacesSummary);
    }

    WorkspaceSummaryDto toSummaryDto(Workspace ws) {
        return new WorkspaceSummaryDto(ws.getId(), ws.getName(), ws.getCity(), ws.getAddress(),
                ws.getPhotos().stream().map(
                        photo -> new WorkspacePhotoDto(photo.getId(), photo.getFilePath(), photo.getOrder())).toList());
    }
}
