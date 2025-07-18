package com.service.sector.aggregator.service;

import com.service.sector.aggregator.data.dto.PendingLandlordDto;
import com.service.sector.aggregator.data.dto.UserStatusChangeRequest;
import com.service.sector.aggregator.data.dto.UserStatusChanged;
import com.service.sector.aggregator.data.dto.WorkspaceSummaryDto;
import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.entity.Workspace;
import com.service.sector.aggregator.data.entity.WorkspacePhoto;
import com.service.sector.aggregator.data.enums.RoleName;
import com.service.sector.aggregator.data.enums.RoleRequestStatus;
import com.service.sector.aggregator.data.enums.WorkspaceStatus;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import com.service.sector.aggregator.data.repositories.WorkspaceRepository;
import com.service.sector.aggregator.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AppUserRepository userRepo;

    @Mock
    private WorkspaceRepository workspaceRepo;

    @InjectMocks
    private AdminServiceImpl adminService;

    private AppUser testUser;
    private Workspace testWorkspace;
    private List<AppUser> testUsers;
    private List<Workspace> testWorkspaces;
    private UserStatusChangeRequest changeRequest;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setPhone("1234567890");
        testUser.setRealName("Test User");
        testUser.setLandlordRoleStatus(RoleRequestStatus.WAITING_APPROVAL);

        // Set up test workspace
        testWorkspace = new Workspace();
        testWorkspace.setId(1L);
        testWorkspace.setName("Test Workspace");
        testWorkspace.setCity("Test City");
        testWorkspace.setAddress("Test Address");
        testWorkspace.setOwner(testUser);
        testWorkspace.setStatus(WorkspaceStatus.UNDER_REVIEW);
        
        // Add photos to workspace
        WorkspacePhoto photo = new WorkspacePhoto();
        photo.setId(1L);
        photo.setFilePath("/test/path.jpg");
        photo.setOrder((short) 1);
        testWorkspace.setPhotos(Collections.singletonList(photo));

        testUsers = Collections.singletonList(testUser);
        testWorkspaces = Collections.singletonList(testWorkspace);
        
        // Set up change request
        changeRequest = new UserStatusChangeRequest(1L);
    }

    @Test
    void getLandlordsByStatus_ShouldReturnMappedLandlords() {
        // Arrange
        when(userRepo.findByLandlordRoleStatus(RoleRequestStatus.WAITING_APPROVAL)).thenReturn(testUsers);
        when(workspaceRepo.findAllByOwner(testUser)).thenReturn(testWorkspaces);

        // Act
        List<PendingLandlordDto> result = adminService.getLandlordsByStatus(RoleRequestStatus.WAITING_APPROVAL);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        PendingLandlordDto dto = result.getFirst();
        assertEquals(1L, dto.userId());
        assertEquals("1234567890", dto.phone());
        assertEquals("Test User", dto.realName());
        assertEquals(1, dto.workspaces().size());
        
        WorkspaceSummaryDto workspaceDto = dto.workspaces().getFirst();
        assertEquals(1L, workspaceDto.id());
        assertEquals("Test Workspace", workspaceDto.name());
        assertEquals("Test City", workspaceDto.city());
        assertEquals("Test Address", workspaceDto.address());
        assertEquals(1, workspaceDto.photos().size());
        
        verify(userRepo).findByLandlordRoleStatus(RoleRequestStatus.WAITING_APPROVAL);
        verify(workspaceRepo).findAllByOwner(testUser);
    }

    @Test
    void getLandlordsByStatus_WhenNoLandlords_ShouldReturnEmptyList() {
        // Arrange
        when(userRepo.findByLandlordRoleStatus(any())).thenReturn(new ArrayList<>());

        // Act
        List<PendingLandlordDto> result = adminService.getLandlordsByStatus(RoleRequestStatus.WAITING_APPROVAL);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepo).findByLandlordRoleStatus(RoleRequestStatus.WAITING_APPROVAL);
        verify(workspaceRepo, never()).findAllByOwner(any());
    }

    @Test
    void approveLandlord_ShouldUpdateUserAndWorkspaces() {
        // Arrange
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(workspaceRepo.findAllByOwner(testUser)).thenReturn(testWorkspaces);

        // Act
        UserStatusChanged result = adminService.approveLandlord(changeRequest);

        // Assert
        assertEquals(RoleRequestStatus.APPROVED, testUser.getLandlordRoleStatus());
        assertEquals(WorkspaceStatus.APPROVED, testWorkspace.getStatus());
        
        assertEquals(1L, result.userId());
        assertEquals(RoleName.LANDLORD, result.role());
        assertEquals(RoleRequestStatus.WAITING_APPROVAL, result.oldStatus());
        assertEquals(RoleRequestStatus.APPROVED, result.newStatus());
        
        verify(userRepo).findById(1L);
        verify(userRepo).save(testUser);
        verify(workspaceRepo).findAllByOwner(testUser);
        verify(workspaceRepo).saveAll(testWorkspaces);
    }

    @Test
    void approveLandlord_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> adminService.approveLandlord(changeRequest));
        
        assertEquals("User not found", exception.getReason());
        verify(userRepo).findById(1L);
        verify(userRepo, never()).save(any());
        verify(workspaceRepo, never()).findAllByOwner(any());
        verify(workspaceRepo, never()).saveAll(any());
    }

    @Test
    void rejectLandlord_ShouldUpdateUserAndWorkspaces() {
        // Arrange
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(workspaceRepo.findAllByOwner(testUser)).thenReturn(testWorkspaces);

        // Act
        UserStatusChanged result = adminService.rejectLandlord(changeRequest);

        // Assert
        assertEquals(RoleRequestStatus.REJECTED, testUser.getLandlordRoleStatus());
        assertEquals(WorkspaceStatus.REJECTED, testWorkspace.getStatus());
        
        assertEquals(1L, result.userId());
        assertEquals(RoleName.LANDLORD, result.role());
        assertEquals(RoleRequestStatus.WAITING_APPROVAL, result.oldStatus());
        assertEquals(RoleRequestStatus.REJECTED, result.newStatus());
        
        verify(userRepo).findById(1L);
        verify(userRepo).save(testUser);
        verify(workspaceRepo).findAllByOwner(testUser);
        verify(workspaceRepo).saveAll(testWorkspaces);
    }

    @Test
    void rejectLandlord_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> adminService.rejectLandlord(changeRequest));
        
        assertEquals("User not found", exception.getReason());
        verify(userRepo).findById(1L);
        verify(userRepo, never()).save(any());
        verify(workspaceRepo, never()).findAllByOwner(any());
        verify(workspaceRepo, never()).saveAll(any());
    }

    @Test
    void approveLandlord_WithNoWorkspaces_ShouldOnlyUpdateUser() {
        // Arrange
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(workspaceRepo.findAllByOwner(testUser)).thenReturn(Collections.emptyList());

        // Act
        UserStatusChanged result = adminService.approveLandlord(changeRequest);

        // Assert
        assertEquals(RoleRequestStatus.APPROVED, testUser.getLandlordRoleStatus());
        
        assertEquals(1L, result.userId());
        assertEquals(RoleName.LANDLORD, result.role());
        assertEquals(RoleRequestStatus.WAITING_APPROVAL, result.oldStatus());
        assertEquals(RoleRequestStatus.APPROVED, result.newStatus());
        
        verify(userRepo).findById(1L);
        verify(userRepo).save(testUser);
        verify(workspaceRepo).findAllByOwner(testUser);
        verify(workspaceRepo).saveAll(Collections.emptyList());
    }

    @Test
    void rejectLandlord_WithNoWorkspaces_ShouldOnlyUpdateUser() {
        // Arrange
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(workspaceRepo.findAllByOwner(testUser)).thenReturn(Collections.emptyList());

        // Act
        UserStatusChanged result = adminService.rejectLandlord(changeRequest);

        // Assert
        assertEquals(RoleRequestStatus.REJECTED, testUser.getLandlordRoleStatus());
        
        assertEquals(1L, result.userId());
        assertEquals(RoleName.LANDLORD, result.role());
        assertEquals(RoleRequestStatus.WAITING_APPROVAL, result.oldStatus());
        assertEquals(RoleRequestStatus.REJECTED, result.newStatus());
        
        verify(userRepo).findById(1L);
        verify(userRepo).save(testUser);
        verify(workspaceRepo).findAllByOwner(testUser);
        verify(workspaceRepo).saveAll(Collections.emptyList());
    }
}