package com.service.sector.aggregator.service;

import com.service.sector.aggregator.data.dto.ActivationRequest;
import com.service.sector.aggregator.data.dto.AppUserRequest;
import com.service.sector.aggregator.data.dto.AppUserResponse;
import com.service.sector.aggregator.data.dto.auth.AuthResponse;
import com.service.sector.aggregator.data.dto.auth.LoginRequest;
import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.entity.Role;
import com.service.sector.aggregator.data.enums.ActivationStatus;
import com.service.sector.aggregator.data.enums.RoleName;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import com.service.sector.aggregator.data.repositories.RoleRepository;
import com.service.sector.aggregator.service.external.JwtService;
import com.service.sector.aggregator.service.external.SmsOtpService;
import com.service.sector.aggregator.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserServiceImpl}.
 */
class UserServiceImplTest {

    @Mock  private AppUserRepository userRepo;
    @Mock  private RoleRepository    roleRepo;
    @Mock  private JwtService jwtService;
    @Mock  private SmsOtpService smsOtpService;

    @InjectMocks
    private UserServiceImpl service;     // concrete implementation under test

    private final String PHONE = "+15550000001";
    private final String REAL_NAME = "John Doe";
    private final String CODE  = "1234";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================================================================
    // register()
    // =========================================================================
    @Test
    void register_success() {
        when(userRepo.existsByPhone(PHONE)).thenReturn(false);
        Role customerRole = Role.builder().roleName(RoleName.CUSTOMER).build();
        when(roleRepo.findByRoleName(RoleName.CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(smsOtpService.newCode(PHONE)).thenReturn(CODE);
        // repository returns the same entity (id not used in assertions here)
        when(userRepo.save(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));

        AppUserRequest req = new AppUserRequest(PHONE, REAL_NAME);
        AppUserResponse resp = service.register(req);

        assertThat(resp.phone()).isEqualTo(PHONE);
        assertThat(resp.activationStatus()).isEqualTo(ActivationStatus.PENDING);
        verify(smsOtpService).send(eq(PHONE), eq(CODE));
    }

    @Test
    void register_phoneAlreadyExists_throws409() {
        when(userRepo.existsByPhone(PHONE)).thenReturn(true);

        AppUserRequest req = new AppUserRequest(PHONE, REAL_NAME);
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.register(req));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(userRepo, never()).save(any());
    }

    // =========================================================================
    // activateUser()
    // =========================================================================
    @Test
    void activateUser_success() {
        AppUser user = AppUser.builder()
                .phone(PHONE)
                .activationCode(CODE)
                .activationStatus(ActivationStatus.PENDING)
                .build();
        when(userRepo.findByPhone(PHONE)).thenReturn(Optional.of(user));

        ActivationRequest req = new ActivationRequest(PHONE, CODE);
        service.activateUser(req);

        assertThat(user.getActivationStatus()).isEqualTo(ActivationStatus.ACTIVATED);
        assertThat(user.getActivationCode()).isNull();
        verify(userRepo).save(user);
    }

    @Test
    void activateUser_wrongCode_throws400() {
        AppUser user = AppUser.builder()
                .phone(PHONE)
                .activationCode(CODE)
                .activationStatus(ActivationStatus.PENDING)
                .build();
        when(userRepo.findByPhone(PHONE)).thenReturn(Optional.of(user));

        ActivationRequest req = new ActivationRequest(PHONE, "0000");
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.activateUser(req));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userRepo, never()).save(any());
    }

    // =========================================================================
    // login()
    // =========================================================================
    @Test
    void login_success() {
        AppUser user = AppUser.builder()
                .id(1L)
                .phone(PHONE)
                .activationStatus(ActivationStatus.ACTIVATED)
                .roles(Set.of())
                .build();
        when(userRepo.findByPhone(PHONE)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(1L)).thenReturn("jwt-token");

        LoginRequest req = new LoginRequest(PHONE);
        AuthResponse resp = service.login(req);

        assertThat(resp.token()).isEqualTo("jwt-token");
    }

    @Test
    void login_notActivated_throws403() {
        AppUser user = AppUser.builder()
                .phone(PHONE)
                .activationStatus(ActivationStatus.PENDING)
                .build();
        when(userRepo.findByPhone(PHONE)).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest(PHONE);
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.login(req));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(jwtService, never()).generateToken(anyLong());
    }
}
