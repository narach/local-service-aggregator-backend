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
import com.service.sector.aggregator.exceptions.InvalidPhoneNumberException;
import com.service.sector.aggregator.exceptions.SmsDeliveryException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final SmsOtpService smsOtpService;

    @Override
    public AppUserResponse register(AppUserRequest request) {
        if (isPhoneRegistered(request.phone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone already registered");
        }

        Role customerRole = roleRepository.findByRoleName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("Customer role not found"));

        String code = smsOtpService.newCode(request.phone());
        AppUser user = AppUser.builder()
                .phone(request.phone())
                .realName(request.realName())
                .roles(Set.of(customerRole))
                .activationCode(code)
                .activationStatus(ActivationStatus.PENDING)
                .build();

        userRepository.save(user);

        if (!SmsOtpService.isTestPhoneNumber(request.phone())) {
            try {
                smsOtpService.send(request.phone(), code);
            } catch (InvalidPhoneNumberException | SmsDeliveryException e) {
                userRepository.delete(user);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Failed to send verification code: " + e.getMessage());
            }
        }

        return mapToResponse(user);
    }

    @Override
    public void activateUser(ActivationRequest req) {
        AppUser user = userRepository.findByPhone(req.phone())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String code = req.code();
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code is required");
        }

        boolean isValid = code.equals(user.getActivationCode()) ||
                (SmsOtpService.isTestPhoneNumber(user.getPhone()) &&
                        code.equals(SmsOtpService.DEFAULT_TEST_CODE));

        if (!isValid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong activation code");
        }

        user.setActivationStatus(ActivationStatus.ACTIVATED);
        user.setActivationCode(null);
        userRepository.save(user);

    }

    @Override
    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByPhone(request.phone())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (user.getActivationStatus() != ActivationStatus.ACTIVATED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not activated");
        }

        String jwt = jwtService.generateToken(user.getId());
        return new AuthResponse(jwt);
    }

    private boolean isPhoneRegistered(String phone) {
        return userRepository.existsByPhone(phone);
    }

    // Other method implementations...

    private AppUserResponse mapToResponse(AppUser user) {
        return new AppUserResponse(
                user.getId(),
                user.getPhone(),
                user.getActivationStatus(),
                user.getRoles().stream()
                        .map(r -> r.getRoleName().name())
                        .collect(Collectors.toSet()),
                user.getCreatedAt());
    }
}
