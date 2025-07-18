package com.service.sector.aggregator.service.impl;

import com.service.sector.aggregator.data.dto.PendingLandlordDto;
import com.service.sector.aggregator.data.dto.UserStatusChangeRequest;
import com.service.sector.aggregator.data.dto.UserStatusChanged;
import com.service.sector.aggregator.data.dto.WorkspacePhotoDto;
import com.service.sector.aggregator.data.dto.WorkspaceSummaryDto;
import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.entity.Workspace;
import com.service.sector.aggregator.data.enums.RoleName;
import com.service.sector.aggregator.data.enums.RoleRequestStatus;
import com.service.sector.aggregator.data.enums.WorkspaceStatus;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import com.service.sector.aggregator.data.repositories.WorkspaceRepository;
import com.service.sector.aggregator.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AppUserRepository userRepo;
    private final WorkspaceRepository workspaceRepo;

    @Override
    public List<PendingLandlordDto> getLandlordsByStatus(RoleRequestStatus status) {
        List<AppUser> users = userRepo.findByLandlordRoleStatus(status);
        List<PendingLandlordDto> landlordsToApprove = new ArrayList<>();
        users.forEach(user -> landlordsToApprove.add(mapLandlord(user)));
        return landlordsToApprove;
    }

    @Override
    @Transactional
    public UserStatusChanged approveLandlord(UserStatusChangeRequest request) {
        AppUser user = userRepo.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        RoleRequestStatus oldStatus = user.getLandlordRoleStatus();
        RoleRequestStatus newStatus = RoleRequestStatus.APPROVED;
        user.setLandlordRoleStatus(newStatus);
        userRepo.save(user);

        List<Workspace> userWorkspaces = workspaceRepo.findAllByOwner(user);
        userWorkspaces.forEach( workspace -> workspace.setStatus(WorkspaceStatus.APPROVED));
        workspaceRepo.saveAll(userWorkspaces);

        return new UserStatusChanged(user.getId(), RoleName.LANDLORD, oldStatus, newStatus);
    }

    @Override
    @Transactional
    public UserStatusChanged rejectLandlord(UserStatusChangeRequest request) {
        AppUser user = userRepo.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        RoleRequestStatus oldStatus = user.getLandlordRoleStatus();
        RoleRequestStatus newStatus = RoleRequestStatus.REJECTED;
        user.setLandlordRoleStatus(newStatus);
        userRepo.save(user);

        List<Workspace> userWorkspaces = workspaceRepo.findAllByOwner(user);
        userWorkspaces.forEach( workspace -> workspace.setStatus(WorkspaceStatus.REJECTED));
        workspaceRepo.saveAll(userWorkspaces);

        return new UserStatusChanged(user.getId(), RoleName.LANDLORD, oldStatus, newStatus);
    }

    private PendingLandlordDto mapLandlord(AppUser user) {
        List<WorkspaceSummaryDto> workspacesSummary = workspaceRepo.findAllByOwner(user).stream()
                .map(this::toSummaryDto).toList();
        return new PendingLandlordDto(user.getId(), user.getPhone(), user.getRealName(), workspacesSummary);
    }

    private WorkspaceSummaryDto toSummaryDto(Workspace ws) {
        return new WorkspaceSummaryDto(ws.getId(), ws.getName(), ws.getCity(), ws.getAddress(),
                ws.getPhotos().stream().map(
                        photo -> new WorkspacePhotoDto(photo.getId(), photo.getFilePath(), photo.getOrder())).toList());
    }
}