package com.service.sector.aggregator.service;

import com.service.sector.aggregator.data.dto.AppUserRequest;
import com.service.sector.aggregator.data.dto.AppUserResponse;
import com.service.sector.aggregator.data.dto.UserDetailsResponse;
import com.service.sector.aggregator.data.dto.auth.LoginRequest;
import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.entity.AuthCode;
import com.service.sector.aggregator.data.entity.Role;
import com.service.sector.aggregator.data.enums.RoleName;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import com.service.sector.aggregator.data.repositories.AuthCodeRepository;
import com.service.sector.aggregator.data.repositories.RoleRepository;
import com.service.sector.aggregator.service.external.JwtService;
import com.service.sector.aggregator.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserServiceImpl}.
 */
class UserServiceImplTest {

    @Mock  private AppUserRepository userRepo;
    @Mock  private AuthCodeRepository authCodeRepository;
    @Mock  private RoleRepository    roleRepo;
    @Mock  private JwtService jwtService;

    @InjectMocks
    private UserServiceImpl service;     // concrete implementation under test

    private final String PHONE = "+15550000001";
    private final String FIRST_NAME = "John";
    private final String LAST_NAME = "Doe";
    private final String CODE  = "123456";
    private final String WRONG_CODE = "000000";

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
        AuthCode authCode = AuthCode.builder()
                .phone(PHONE)
                .code(CODE)
                .validUntil(OffsetDateTime.now().plusMinutes(10))
                .build();
        when(authCodeRepository.findAllByPhone(PHONE)).thenReturn(List.of(authCode));
        Role customerRole = Role.builder().roleName(RoleName.CUSTOMER).build();
        when(roleRepo.findByRoleName(RoleName.CUSTOMER)).thenReturn(Optional.of(customerRole));
        // repository returns the same entity (id not used in assertions here)
        when(userRepo.save(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));

        AppUserRequest req = new AppUserRequest(PHONE, FIRST_NAME, LAST_NAME, CODE);
        AppUserResponse resp = service.register(req);

        assertThat(resp.phone()).isEqualTo(PHONE);
        assertThat(resp.firstName()).isEqualTo(FIRST_NAME);
        assertThat(resp.lastName()).isEqualTo(LAST_NAME);
    }

    @Test
    void register_phoneAlreadyExists_throws409() {
        when(userRepo.existsByPhone(PHONE)).thenReturn(true);

        AppUserRequest req = new AppUserRequest(PHONE, FIRST_NAME, LAST_NAME, CODE);
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.register(req));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(userRepo, never()).save(any());
    }

    @Test
    void register_invalidSmsCode_throws400() {
        when(userRepo.existsByPhone(PHONE)).thenReturn(false);
        AuthCode expiredCode = AuthCode.builder()
                .phone(PHONE)
                .code(CODE)
                .validUntil(OffsetDateTime.now().minusMinutes(1))
                .build();
        when(authCodeRepository.findAllByPhone(PHONE)).thenReturn(List.of(expiredCode));

        AppUserRequest req = new AppUserRequest(PHONE, FIRST_NAME, LAST_NAME, CODE);
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.register(req));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).contains("Invalid or expired smsCode");
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
                 .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .build();
        // user exists
        when(userRepo.findByPhone(PHONE)).thenReturn(Optional.of(user));

        // matching verification-code record exists
        AuthCode validProvidedCode = AuthCode.builder()
                .phone(PHONE)
                .code(CODE)
                .validUntil(OffsetDateTime.now().plusMinutes(10))
                .build();
        AuthCode expiredCode = AuthCode.builder()
                .phone(PHONE)
                .code("222222")
                .validUntil(OffsetDateTime.now().minusMinutes(5))
                .build();
        when(authCodeRepository.findAllByPhone(PHONE)).thenReturn(List.of(validProvidedCode, expiredCode));

        // token generation succeeds
        when(jwtService.generateToken(user)).thenReturn("dummy-jwt");

        AppUserResponse resp = service.login(new LoginRequest(PHONE, CODE));

        assertThat(resp.token()).isEqualTo("dummy-jwt");
        verify(authCodeRepository).deleteAll(argThat(codes ->
                java.util.stream.StreamSupport.stream(codes.spliterator(), false)
                        .map(AuthCode::getCode)
                        .collect(java.util.stream.Collectors.toSet())
                        .containsAll(java.util.Set.of(CODE, "222222"))));
    }

    @Test
    void login_wrong_code_401() {
        AppUser user = AppUser.builder()
                .phone(PHONE)
                 .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .build();
        AuthCode authCode = AuthCode.builder().phone(PHONE).code(CODE).build();

        when(userRepo.findByPhone(PHONE)).thenReturn(Optional.of(user));
        when(authCodeRepository.findAllByPhone(PHONE)).thenReturn(List.of(authCode));

        LoginRequest req = new LoginRequest(PHONE, WRONG_CODE);
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.login(req));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(jwtService, never()).generateToken(user);
    }

    @Test
    void login_expired_code_401() {
        AppUser user = AppUser.builder()
                .phone(PHONE)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .build();
        AuthCode expiredCode = AuthCode.builder()
                .phone(PHONE)
                .code(CODE)
                .validUntil(OffsetDateTime.now().minusMinutes(1))
                .build();

        when(userRepo.findByPhone(PHONE)).thenReturn(Optional.of(user));
        when(authCodeRepository.findAllByPhone(PHONE)).thenReturn(List.of(expiredCode));

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.login(new LoginRequest(PHONE, CODE)));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getReason()).contains("expired");
        verify(jwtService, never()).generateToken(user);
    }

    @Test
    void getUserByPhone_success_createsAuthCodeAndReturnsDetails() {
        Role customerRole = Role.builder().roleName(RoleName.CUSTOMER).build();
        AppUser user = AppUser.builder()
                .phone(PHONE)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .roles(java.util.Set.of(customerRole))
                .build();
        when(userRepo.findByPhone(PHONE)).thenReturn(Optional.of(user));

        UserDetailsResponse resp = service.getUserByPhone(PHONE);

        assertThat(resp.phone()).isEqualTo(PHONE);
        assertThat(resp.firstName()).isEqualTo(FIRST_NAME);
        assertThat(resp.lastName()).isEqualTo(LAST_NAME);
        assertThat(resp.roles()).contains("CUSTOMER");
        verify(authCodeRepository).save(any(AuthCode.class));
    }

    @Test
    void getUserByPhone_userNotFound_initializesRegistrationFlowAndThrows404() {
        when(userRepo.findByPhone(PHONE)).thenReturn(Optional.empty());

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.getUserByPhone(PHONE));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(authCodeRepository).deleteAllByPhone(PHONE);
        verify(authCodeRepository).save(argThat(c ->
                PHONE.equals(c.getPhone()) && UserServiceImpl.REGISTRATION_INIT_CODE.equals(c.getCode())));
    }
}

