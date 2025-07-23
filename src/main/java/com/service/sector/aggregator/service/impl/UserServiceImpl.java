package com.service.sector.aggregator.service.impl;

import com.service.sector.aggregator.data.dto.AppUserRequest;
import com.service.sector.aggregator.data.dto.AppUserResponse;
import com.service.sector.aggregator.data.dto.auth.LoginRequest;
import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.entity.AuthCode;
import com.service.sector.aggregator.data.entity.Role;
import com.service.sector.aggregator.data.enums.RoleName;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import com.service.sector.aggregator.data.repositories.AuthCodeRepository;
import com.service.sector.aggregator.data.repositories.RoleRepository;
import com.service.sector.aggregator.service.UserService;
import com.service.sector.aggregator.service.external.JwtService;
import com.service.sector.aggregator.service.external.SmsOtpService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.utils.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    public static final String DEFAULT_TEST_CODE = "123456";

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthCodeRepository authCodeRepository;

    private final JwtService jwtService;
    private final SmsOtpService smsOtpService;

    @Override
    public AppUserResponse register(AppUserRequest request) {
        if (isPhoneRegistered(request.phone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone already registered");
        }

        Role customerRole = roleRepository.findByRoleName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("Customer role not found"));

        AppUser user = AppUser.builder()
                .phone(request.phone())
                .realName(request.realName())
                .roles(Set.of(customerRole))
                .build();

        Optional<AuthCode> authCode = authCodeRepository.findByPhone(request.phone());

        userRepository.save(user);

        // Cleanup auth code attempts
        authCode.ifPresent(authCodeRepository::delete);
        String authToken = jwtService.generateToken(user);

        return mapToResponse(user, authToken);
    }

    @Override
    public AppUserResponse login(LoginRequest request) {
        AppUser user = userRepository.findByPhone(request.phone())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No Such user"));

        List<AuthCode> codes = authCodeRepository.findAllByPhone(request.phone());

        if (codes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid auth code");
        }

        boolean anyMatch = codes.stream()
                .anyMatch(c -> StringUtils.equals(c.getCode(), request.code()));

        if (!anyMatch) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid auth code");
        }

        // at least one code matches – issue token
        String jwt = jwtService.generateToken(user);

        // cleanup: remove EVERY auth-code record for this phone
        authCodeRepository.deleteAllByPhone(request.phone());

        return mapToResponse(user, jwt);
    }

    @Override
    public void sendCode(String phone) {
        String code;
        if (!SmsOtpService.isTestPhoneNumber(phone)) {
            code = smsOtpService.newCode(phone);
            smsOtpService.send(phone, code);
        } else {
            code = DEFAULT_TEST_CODE;
        }
        authCodeRepository.deleteAllByPhone(phone);
        authCodeRepository.save(AuthCode.builder().phone(phone).code(code).build());
    }

    @Override
    public AppUser getUserDetails(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private boolean isPhoneRegistered(String phone) {
        return userRepository.existsByPhone(phone);
    }

    // Other method implementations...
    private AppUserResponse mapToResponse(AppUser user, String authToken) {
        return new AppUserResponse(
                user.getId(),
                user.getPhone(),
                user.getRealName(),
                user.getRoles().stream()
                        .map(r -> r.getRoleName().name())
                        .collect(Collectors.toSet()),
                user.getCreatedAt(),
                authToken);
    }
}
