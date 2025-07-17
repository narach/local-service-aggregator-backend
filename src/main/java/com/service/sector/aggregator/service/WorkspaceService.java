package com.service.sector.aggregator.service;

import com.service.sector.aggregator.data.dto.BecomeLandlordResponse;
import com.service.sector.aggregator.data.dto.WorkspaceResponse;
import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.entity.Workspace;
import com.service.sector.aggregator.data.form.WorkspaceForm;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface WorkspaceService {
    BecomeLandlordResponse requestLandlord(Long userId, WorkspaceForm form, List<MultipartFile> photos);
    WorkspaceResponse createWorkspace(Long userId, WorkspaceForm form, List<MultipartFile> photos);
    List<Workspace> getWorkspacesByOwner(AppUser owner);
    List<Workspace> getWorkspacesByOwnerId(Long ownerId);
}

