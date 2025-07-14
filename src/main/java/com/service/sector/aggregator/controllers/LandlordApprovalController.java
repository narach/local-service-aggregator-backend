package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.enums.RoleRequestStatus;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Workspace‑related admin operations.
 */
@Tag(name = "Admin • Workspaces", description = "Landlord approval operations")
@RestController
@RequestMapping("/api/admin/landlords")
@RequiredArgsConstructor
public class LandlordApprovalController {

    private final AppUserRepository userRepo;

    /**
     * GET /api/admin/landlords/pending – list users whose landlord role is waiting approval.
     */
    @Operation(summary = "List landlord requests awaiting approval")
    @GetMapping("/pending")
    public ResponseEntity<List<AppUser>> listPendingLandlords() {
        List<AppUser> pending = userRepo.findByLandlordRoleStatus(RoleRequestStatus.WAITING_APPROVAL);
        return ResponseEntity.ok(pending);
    }
}
