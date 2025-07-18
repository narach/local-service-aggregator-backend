
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
import com.service.sector.aggregator.service.impl.WorkspaceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceImplTest {

    @Mock
    private ImageService imageSrv;

    @Mock
    private S3Service s3Srv;

    @Mock
    private DateTimeService dtSrv;

    @Mock
    private AppUserRepository userRepo;

    @Mock
    private WorkspaceRepository workspaceRepo;

    @InjectMocks
    private WorkspaceServiceImpl workspaceService;

    private AppUser testUser;
    private WorkspaceForm testForm;
    private Workspace testWorkspace;

    @BeforeEach
    void setUp() {
        testUser = AppUser.builder()
                .id(1L)
                .realName("John Doe")
                .landlordRoleStatus(RoleRequestStatus.NO)
                .build();

        testForm = new WorkspaceForm(
                "Test Workspace",
                "Test City",
                "Test Address",
                "Office Space",
                "Test Description",
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"),
                60,
                new BigDecimal("25.00"),
                "Test Legal Name",
                "REG123456",
                "Legal details"
        );

        testWorkspace = Workspace.builder()
                .id(1L)
                .owner(testUser)
                .name("Test Workspace")
                .city("Test City")
                .photos(new ArrayList<>())
                .build();
    }

    @Test
    void requestLandlord_Success() throws IOException {
        // Arrange
        List<MultipartFile> testPhotos = createFullyMockedMultipartFiles();
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepo.save(any(AppUser.class))).thenReturn(testUser);
        when(dtSrv.toMask(anyList())).thenReturn((short) 31);
        when(imageSrv.compress(any(byte[].class))).thenReturn(new byte[]{1, 2, 3});
        when(s3Srv.upload(any(byte[].class), anyString(), anyString())).thenReturn("https://s3.example.com/photo.jpg");
        when(workspaceRepo.save(any(Workspace.class))).thenReturn(testWorkspace);

        // Act
        BecomeLandlordResponse result = workspaceService.requestLandlord(1L, testForm, testPhotos);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.userId());
        assertEquals("John Doe", result.userName());
        assertEquals(RoleRequestStatus.WAITING_APPROVAL, result.landlordStatus());
        assertNotNull(result.workspaceResponse());

        verify(userRepo).findById(1L);
        verify(userRepo).save(argThat(user -> user.getLandlordRoleStatus() == RoleRequestStatus.WAITING_APPROVAL));
        verify(workspaceRepo).save(any(Workspace.class));
    }

    @Test
    void requestLandlord_UserNotFound() {
        // Arrange
        List<MultipartFile> testPhotos = createBasicMockMultipartFiles(3);
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> workspaceService.requestLandlord(1L, testForm, testPhotos));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(userRepo).findById(1L);
        verify(userRepo, never()).save(any(AppUser.class));
    }

    // replace the whole body of requestLandlord_PhotoProcessingError
    @Test
    void requestLandlord_PhotoProcessingError() throws IOException {
        // Arrange
        List<MultipartFile> testPhotos = IntStream.range(0, 3)
                .mapToObj(i -> (MultipartFile) new MockMultipartFile(
                        "photo" + i,
                        "photo" + i + ".jpg",
                        "image/jpeg",
                        new byte[]{10, 20, 30}))
                .collect(Collectors.toList());


        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepo.save(any(AppUser.class))).thenReturn(testUser);
        when(dtSrv.toMask(anyList())).thenReturn((short) 31);
        when(imageSrv.compress(any(byte[].class))).thenThrow(new IOException("Compression failed"));

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> workspaceService.requestLandlord(1L, testForm, testPhotos));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
        assertEquals("Failed to process workspace photos", ex.getReason());
    }

    @Test
    void createWorkspace_Success() throws IOException {
        // Arrange
        List<MultipartFile> testPhotos = createFullyMockedMultipartFiles();
        testUser.setLandlordRoleStatus(RoleRequestStatus.APPROVED);

        // Create a workspace with proper photos for the response
        List<WorkspacePhoto> mockPhotos = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            WorkspacePhoto photo = WorkspacePhoto.builder()
                    .filePath("https://s3.example.com/photo" + i + ".jpg")
                    .order((short) i)
                    .build();
            mockPhotos.add(photo);
        }

        // Update testWorkspace to have photos
        testWorkspace.setPhotos(mockPhotos);

        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(dtSrv.toMask(anyList())).thenReturn((short) 31);
        when(imageSrv.compress(any(byte[].class))).thenReturn(new byte[]{1, 2, 3});
        when(s3Srv.upload(any(byte[].class), anyString(), anyString())).thenReturn("https://s3.example.com/photo.jpg");

        // Mock the save method to return a workspace with ID and photos
        when(workspaceRepo.save(any(Workspace.class))).thenAnswer(invocation -> {
            Workspace savedWorkspace = invocation.getArgument(0);
            // Simulate the database setting the ID
            savedWorkspace.setId(1L);
            return savedWorkspace;
        });

        // Act
        WorkspaceResponse result = workspaceService.createWorkspace(1L, testForm, testPhotos);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test Workspace", result.name());
        assertEquals("Test City", result.city());
        assertNotNull(result.photoUrls());
        assertEquals(3, result.photoUrls().size());

        verify(userRepo).findById(1L);
        verify(workspaceRepo).save(argThat(workspace -> workspace.getStatus() == WorkspaceStatus.APPROVED));
    }

    @Test
    void createWorkspace_UserNotFound() {
        // Arrange
        List<MultipartFile> testPhotos = createBasicMockMultipartFiles(3);
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> workspaceService.createWorkspace(1L, testForm, testPhotos));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(userRepo).findById(1L);
        verify(workspaceRepo, never()).save(any(Workspace.class));
    }

    @Test
    void createWorkspace_UserNotApproved() {
        // Arrange
        List<MultipartFile> testPhotos = createBasicMockMultipartFiles(3);
        testUser.setLandlordRoleStatus(RoleRequestStatus.NO);
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> workspaceService.createWorkspace(1L, testForm, testPhotos));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("User is not approved as a landlord", exception.getReason());
        verify(userRepo).findById(1L);
        verify(workspaceRepo, never()).save(any(Workspace.class));
    }

    @Test
    void createWorkspace_UserWaitingApproval() {
        // Arrange
        List<MultipartFile> testPhotos = createBasicMockMultipartFiles(3);
        testUser.setLandlordRoleStatus(RoleRequestStatus.WAITING_APPROVAL);
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> workspaceService.createWorkspace(1L, testForm, testPhotos));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("User is not approved as a landlord", exception.getReason());
        verify(userRepo).findById(1L);
        verify(workspaceRepo, never()).save(any(Workspace.class));
    }

    @Test
    void createWorkspace_UserRejected() {
        // Arrange
        List<MultipartFile> testPhotos = createBasicMockMultipartFiles(3);
        testUser.setLandlordRoleStatus(RoleRequestStatus.REJECTED);
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> workspaceService.createWorkspace(1L, testForm, testPhotos));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("User is not approved as a landlord", exception.getReason());
        verify(userRepo).findById(1L);
        verify(workspaceRepo, never()).save(any(Workspace.class));
    }

    // replace the whole body of createWorkspace_PhotoProcessingError
    @Test
    void createWorkspace_PhotoProcessingError() throws IOException {
        // Arrange
        List<MultipartFile> testPhotos = IntStream.range(0, 3)
                .mapToObj(i -> (MultipartFile) new MockMultipartFile(
                        "photo" + i,
                        "photo" + i + ".jpg",
                        "image/jpeg",
                        new byte[]{10, 20, 30}))
                .collect(Collectors.toList());


        testUser.setLandlordRoleStatus(RoleRequestStatus.APPROVED);
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(dtSrv.toMask(anyList())).thenReturn((short) 31);
        when(imageSrv.compress(any(byte[].class))).thenThrow(new IOException("Compression failed"));

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> workspaceService.createWorkspace(1L, testForm, testPhotos));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
        assertEquals("Failed to process workspace photos", ex.getReason());
    }

    @Test
    void createWorkspace_TooFewPhotos() {
        // Arrange
        List<MultipartFile> tooFewPhotos = createBasicMockMultipartFiles(2);
        testUser.setLandlordRoleStatus(RoleRequestStatus.APPROVED);
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(dtSrv.toMask(anyList())).thenReturn((short) 31);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> workspaceService.createWorkspace(1L, testForm, tooFewPhotos));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("3–15 photos required", exception.getReason());
    }

    @Test
    void createWorkspace_TooManyPhotos() {
        // Arrange
        List<MultipartFile> tooManyPhotos = createBasicMockMultipartFiles(16);
        testUser.setLandlordRoleStatus(RoleRequestStatus.APPROVED);
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(dtSrv.toMask(anyList())).thenReturn((short) 31);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> workspaceService.createWorkspace(1L, testForm, tooManyPhotos));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("3–15 photos required", exception.getReason());
    }

    @Test
    void getWorkspacesByOwner_Success() {
        // Arrange
        List<Workspace> expectedWorkspaces = Collections.singletonList(testWorkspace);
        when(workspaceRepo.findAllByOwner(testUser)).thenReturn(expectedWorkspaces);

        // Act
        List<Workspace> result = workspaceService.getWorkspacesByOwner(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWorkspace, result.getFirst());
        verify(workspaceRepo).findAllByOwner(testUser);
    }

    @Test
    void getWorkspacesByOwnerId_Success() {
        // Arrange
        List<Workspace> expectedWorkspaces = Collections.singletonList(testWorkspace);
        when(workspaceRepo.findAllByOwnerId(1L)).thenReturn(expectedWorkspaces);

        // Act
        List<Workspace> result = workspaceService.getWorkspacesByOwnerId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWorkspace, result.getFirst());
        verify(workspaceRepo).findAllByOwnerId(1L);
    }

    @Test
    void getWorkspacesByOwner_EmptyList() {
        // Arrange
        when(workspaceRepo.findAllByOwner(testUser)).thenReturn(Collections.emptyList());

        // Act
        List<Workspace> result = workspaceService.getWorkspacesByOwner(testUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(workspaceRepo).findAllByOwner(testUser);
    }

    @Test
    void getWorkspacesByOwnerId_EmptyList() {
        // Arrange
        when(workspaceRepo.findAllByOwnerId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<Workspace> result = workspaceService.getWorkspacesByOwnerId(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(workspaceRepo).findAllByOwnerId(1L);
    }

    /**
     * Creates basic mock MultipartFile objects with no stubs.
     * Use this for tests that don't process file content at all.
     */
    private List<MultipartFile> createBasicMockMultipartFiles(int count) {
        List<MultipartFile> files = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MultipartFile mockFile = mock(MultipartFile.class);
            files.add(mockFile);
        }
        return files;
    }

    /**
     * Creates fully mocked MultipartFile objects with all necessary stubs.
     * Use this for tests that will process files completely (including S3 upload).
     */
    private List<MultipartFile> createFullyMockedMultipartFiles() {
        List<MultipartFile> files = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            MultipartFile mockFile = mock(MultipartFile.class);
            try {
                when(mockFile.getBytes()).thenReturn("test content".getBytes());
                when(mockFile.getContentType()).thenReturn("image/jpeg");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            files.add(mockFile);
        }
        return files;
    }
}