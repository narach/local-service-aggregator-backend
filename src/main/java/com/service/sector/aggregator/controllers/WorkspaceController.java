package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.dto.BecomeLandlordResponse;
import com.service.sector.aggregator.data.dto.WorkspaceResponse;
import com.service.sector.aggregator.data.form.WorkspaceForm;
import com.service.sector.aggregator.service.WorkspaceService;
import com.service.sector.aggregator.service.external.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Workspace CRUD endpoints.
 */
@Tag(name = "Landlord", description = "Landlord functionality")
@RestController
@RequestMapping("/landlord")
@RequiredArgsConstructor
public class WorkspaceController {

    private final JwtService jwt;
    private final WorkspaceService workspaceService;

    @Operation(summary = "Request to become a landlord")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Request created",
                    content = @Content(schema = @Schema(implementation = WorkspaceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping(value = "/request-landlord", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BecomeLandlordResponse> requestLandlord(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @Valid WorkspaceForm form,
            @RequestPart("photos") List<MultipartFile> photos) {

        Long uid = jwt.extractUserId(auth);
        BecomeLandlordResponse response = workspaceService.requestLandlord(uid, form, photos);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Register new workspace")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Workspace created",
                    content = @Content(schema = @Schema(implementation = WorkspaceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PostMapping(value = "/add-workspace", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WorkspaceResponse> create(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @Valid WorkspaceForm form,
            @RequestPart("photos") List<MultipartFile> photos) {

        Long uid = jwt.extractUserId(auth);
        WorkspaceResponse response = workspaceService.createWorkspace(uid, form, photos);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}