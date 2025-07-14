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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
