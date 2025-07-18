package com.service.sector.aggregator.service;

import com.service.sector.aggregator.data.dto.BecomeLandlordResponse;
import com.service.sector.aggregator.data.dto.WorkspaceResponse;
import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.entity.Workspace;
import com.service.sector.aggregator.data.entity.WorkspacePhoto;
import com.service.sector.aggregator.data.enums.RoleRequestStatus;
import com.service.sector.aggregator.data.enums.WorkspaceStatus;
import com.service.sector.aggregator.data.form.WorkspaceForm;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import com.service.sector.aggregator.data.repositories.WorkspaceRepository;
import com.service.sector.aggregator.service.external.DateTimeService;
import com.service.sector.aggregator.service.external.ImageService;
import com.service.sector.aggregator.service.external.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {
    
    private final ImageService imageSrv;
    private final S3Service s3Srv;
    private final DateTimeService dtSrv;
    private final AppUserRepository userRepo;
    private final WorkspaceRepository workspaceRepo;

    @Override
    @Transactional
    public BecomeLandlordResponse requestLandlord(Long userId, WorkspaceForm form, List<MultipartFile> photos) {
        try {
            // 1. Update workspace owner status to waiting approval == request to become a workspace owner
            AppUser user = userRepo.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
            user.setLandlordRoleStatus(RoleRequestStatus.WAITING_APPROVAL);
            userRepo.save(user);

            // 2. Process first workspace data uploading
            Workspace ws = createWorkspaceEntity(user, form, photos);
            return new BecomeLandlordResponse(user.getId(), user.getRealName(), user.getLandlordRoleStatus(),
                    new WorkspaceResponse(ws.getId(), ws.getName(), ws.getCity(),
                            ws.getPhotos().stream().map(WorkspacePhoto::getFilePath).toList()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process workspace photos", e);
        }
    }

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(Long userId, WorkspaceForm form, List<MultipartFile> photos) {
        try {
            // 1. Authenticated user
            AppUser owner = userRepo.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

            // 2. Check if user is an approved workspace owner.
            if (owner.getLandlordRoleStatus() != RoleRequestStatus.APPROVED) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not approved as a landlord");
            }

            // 3. build Workspace
            Workspace ws = createWorkspaceEntity(owner, form, photos);
            // 4. if the user is an approved landlord- all his new workspaces are approved by default
            ws.setStatus(WorkspaceStatus.APPROVED);

            return new WorkspaceResponse(ws.getId(), ws.getName(), ws.getCity(),
                    ws.getPhotos().stream().map(WorkspacePhoto::getFilePath).toList());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process workspace photos", e);
        }
    }

    @Override
    public List<Workspace> getWorkspacesByOwner(AppUser owner) {
        return workspaceRepo.findAllByOwner(owner);
    }

    @Override
    public List<Workspace> getWorkspacesByOwnerId(Long ownerId) {
        return workspaceRepo.findAllByOwnerId(ownerId);
    }

    private Workspace createWorkspaceEntity(AppUser owner, WorkspaceForm form, List<MultipartFile> photos) throws IOException {
        // build Workspace
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