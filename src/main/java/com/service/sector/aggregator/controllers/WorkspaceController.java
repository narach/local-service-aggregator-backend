package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.dto.WorkspaceResponse;
import com.service.sector.aggregator.data.entity.*;
import com.service.sector.aggregator.data.enums.Status;
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
@Tag(name = "Workspaces", description = "Workspace registration and management")
@RestController
@RequestMapping("/workspace")
@RequiredArgsConstructor
public class WorkspaceController {

    private final JwtService jwt;
    private final ImageService imageSrv;
    private final S3Service s3Srv;
    private final DateTimeService dtSrv;
    private final AppUserRepository userRepo;
    private final WorkspaceRepository workspaceRepo;

    @Operation(summary = "Register new workspace")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Workspace created",
                    content = @Content(schema = @Schema(implementation = WorkspaceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<WorkspaceResponse> create(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @Valid WorkspaceForm form,
            @RequestPart("photos") List<MultipartFile> photos) throws IOException {

        if (photos.size() < 3 || photos.size() > 15)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "3–15 photos required");

        // 1. owner
        Long uid = jwt.extractUserId(auth);
        AppUser owner = userRepo.findById(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

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
                .status(Status.UNDER_REVIEW)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        // 3. photos
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
        ws.getPhotos().addAll(photoEntities);

        workspaceRepo.save(ws); // cascade will save photos

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new WorkspaceResponse(ws.getId(), ws.getName(), ws.getCity(), photoEntities.stream().map(WorkspacePhoto::getFilePath).toList()));
    }

}
