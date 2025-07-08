package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.entity.*;
import com.service.sector.aggregator.data.form.WorkspaceForm;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import com.service.sector.aggregator.data.repositories.WorkspaceRepository;
import com.service.sector.aggregator.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
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

    private static final String BUCKET = "workplace-photos";
    private static final long MAX_SIZE_BYTES = 1_000_000; // 1 MB

    private final WorkspaceRepository workspaceRepository;
    private final AppUserRepository userRepository;
    private final JwtService jwtService;
    private final S3Client s3;

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
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid WorkspaceForm form,
            @RequestPart("photos") MultipartFile[] photos) throws IOException {

        Long userId = extractUserId(authorization);
        AppUser owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (photos == null || photos.length < 3 || photos.length > 15) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide 3-15 photos");
        }

        short workingMask = toMask(form.workingDays());

        // Persist workspace entity first (status DRAFT until moderation)
        Workspace workspace = Workspace.builder()
                .owner(owner)
                .name(form.name())
                .city(form.city())
                .address(form.address())
                .kind(form.kind())
                .description(form.description())
                .openTime(form.openTime())
                .closeTime(form.closeTime())
                .workingDaysMask(workingMask)
                .minRentMinutes(form.minRentMinutes())
                .pricePerHour(form.pricePerHour())
                .legalName(form.legalName())
                .legalRegistrationNo(form.legalRegistrationNo())
                .legalDetails(form.legalDetails())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .status(Workspace.Status.UNDER_REVIEW)
                .build();

        workspaceRepository.save(workspace);

        // Upload photos + create WorkspacePhoto entities
        List<WorkspacePhoto> photoEntities = new ArrayList<>();
        short order = 0;
        for (MultipartFile file : photos) {
            byte[] compressed = compress(file);
            String key = "workspace/" + workspace.getId() + "/" + UUID.randomUUID() + ".jpg";

            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(key)
                    .contentType("image/jpeg")
                    .build();
            s3.putObject(putReq,
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(compressed));

            String url = "https://" + BUCKET + ".s3.eu-north-1.amazonaws.com/" + key;

            WorkspacePhoto photo = WorkspacePhoto.builder()
                    .workspace(workspace)
                    .filePath(url)
                    .order(order++)
                    .createdAt(OffsetDateTime.now())
                    .build();
            photoEntities.add(photo);
        }

        workspace.getPhotos().addAll(photoEntities);
        workspaceRepository.save(workspace); // cascade will save photos

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new WorkspaceResponse(workspace.getId(), workspace.getName(), workspace.getCity(), photoEntities.stream().map(WorkspacePhoto::getFilePath).toList()));
    }

    // =====================================================================
    // Helpers
    // =====================================================================
    private Long extractUserId(String authHeader) {
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Bearer token");
        }
        String token = authHeader.substring(7);
        try {
            return jwtService.parseUserId(token);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT");
        }
    }

    private short toMask(List<String> days) {
        short mask = 0;
        if (days == null) return mask;
        for (String d : days) {
            if (!StringUtils.hasText(d)) continue;
            DayOfWeek dow;
            try {
                dow = DayOfWeek.valueOf(d.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid day: " + d);
            }
            int bit = dow.getValue() % 7; // Monday=1..Sunday=7 → bit 0..6
            mask |= (1 << (bit - 1));
        }
        return mask;
    }

    /**
     * Compress an image to JPEG under 1 MB.
     */
    private static byte[] compress(MultipartFile file) throws IOException {
        if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files allowed");
        }
        var in = file.getInputStream();
        var img = javax.imageio.ImageIO.read(in);
        if (img == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot read image");

        float quality = 0.85f;
        byte[] data;
        do {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Thumbnails.of(img)
                    .scale(1.0)
                    .outputFormat("jpg")
                    .outputQuality(quality)
                    .toOutputStream(baos);
            data = baos.toByteArray();
            quality -= 0.05f;
        } while (data.length > MAX_SIZE_BYTES && quality > 0.30f);

        if (data.length > MAX_SIZE_BYTES)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image too large after compression");
        return data;
    }

    // ---------- DTOs ----------



    public record WorkspaceResponse(Long id, String name, String city, List<String> photoUrls) {}
}
