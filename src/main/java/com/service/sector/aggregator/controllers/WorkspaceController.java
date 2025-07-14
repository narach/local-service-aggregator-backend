package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.dto.BecomeLandlordResponse;
import com.service.sector.aggregator.data.dto.WorkspaceResponse;
import com.service.sector.aggregator.data.entity.*;
import com.service.sector.aggregator.data.enums.RoleRequestStatus;
import com.service.sector.aggregator.data.enums.WorkspaceStatus;
import com.service.sector.aggregator.data.form.WorkspaceForm;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import com.service.sector.aggregator.data.repositories.WorkspaceRepository;
import com.service.sector.aggregator.service.DateTimeService;
import com.service.sector.aggregator.service.ImageService;
import com.service.sector.aggregator.service.JwtService;
import com.service.sector.aggregator.service.S3Service;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Workspace CRUD endpoints.
 */
@Tag(name = "Landlord", description = "Landlord functionality")
@RestController
@RequestMapping("/landlord")
@RequiredArgsConstructor
public class WorkspaceController {

    private final JwtService jwt;
    private final ImageService imageSrv;
    private final S3Service s3Srv;
    private final DateTimeService dtSrv;
    private final AppUserRepository userRepo;
    private final WorkspaceRepository workspaceRepo;

    @Operation(summary = "Request to become a landlord")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Request created",
                    content = @Content(schema = @Schema(implementation = WorkspaceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping(value = "/request-landlord", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<BecomeLandlordResponse> requestLandlord(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @Valid WorkspaceForm form,
            @RequestPart("photos") List<MultipartFile> photos) throws IOException {

        // 1. Update workspace owner status to waiting approval == request to become a workspace owner
        Long uid = jwt.extractUserId(auth);
        AppUser user = userRepo.findById(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        user.setLandlordRoleStatus(RoleRequestStatus.WAITING_APPROVAL);
        userRepo.save(user);

        // 2. Process first workspace data uploading
        Workspace ws = createWorkspace(user, form, photos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BecomeLandlordResponse(user.getId(), user.getRealName(), user.getLandlordRoleStatus(),
                        new WorkspaceResponse(ws.getId(), ws.getName(), ws.getCity(),
                            ws.getPhotos().stream().map(WorkspacePhoto::getFilePath).toList())));
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
    @Transactional
    public ResponseEntity<WorkspaceResponse> create(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @Valid WorkspaceForm form,
            @RequestPart("photos") List<MultipartFile> photos) throws IOException {

        // 1. Authenticated user
        Long uid = jwt.extractUserId(auth);
        AppUser owner = userRepo.findById(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        // 2. Check if user is an approved workspace owner.
        if (owner.getLandlordRoleStatus() != RoleRequestStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not approved as a landlord");
        }

        // 2. build Workspace
        Workspace ws = createWorkspace(owner, form, photos);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new WorkspaceResponse(ws.getId(), ws.getName(), ws.getCity(),
                        ws.getPhotos().stream().map(WorkspacePhoto::getFilePath).toList()));
    }


    private Workspace createWorkspace(AppUser owner, WorkspaceForm form, List<MultipartFile> photos) throws IOException {
        // 2. build Workspace
        Workspace ws = Workspace.builder()
                .owner(owner)
                .name(form.name())
                .city(form.city())
                .address(form.address())
                .kind(form.kind())
                .description(form.description())
                .openTime(form.openTime())
                .closeTime(form.closeTime())
                .workingDaysMask(dtSrv.toMask(form.workingDays()))
                .minRentMinutes(form.minRentMinutes())
                .pricePerHour(form.pricePerHour())
                .status(WorkspaceStatus.UNDER_REVIEW)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        ws.getPhotos().addAll(getWorkspacePhotos(photos, ws));

        workspaceRepo.save(ws); // cascade will save photos
        return ws;
    }

    // Process uploaded photos
    private List<WorkspacePhoto> getWorkspacePhotos(List<MultipartFile> photos, Workspace ws) throws IOException {
        if (photos.size() < 3 || photos.size() > 15)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "3–15 photos required");

        List<WorkspacePhoto> photoEntities = new ArrayList<>();
        for (int i = 0; i < photos.size(); i++) {
            MultipartFile mf = photos.get(i);
            byte[] compressed = imageSrv.compress(mf.getBytes());
            String key = "workspace/%d/%d.jpg".formatted(ws.getId() == null ? 0 : ws.getId(), i);
            String url = s3Srv.upload(compressed, key, mf.getContentType());

            WorkspacePhoto p = WorkspacePhoto.builder()
                    .workspace(ws)
                    .filePath(url)
                    .order((short) i)
                    .createdAt(OffsetDateTime.now())
                    .build();
            photoEntities.add(p);
        }

        return photoEntities;
    }

}
