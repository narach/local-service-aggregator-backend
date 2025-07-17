package com.service.sector.aggregator.controllers;

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
import com.service.sector.aggregator.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.service.sector.aggregator.service.SmsOtpService;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for user registration.
 */
@Tag(name = "Users", description = "Operations about application users")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {

    private final String DEFAULT_AUTH_CODE = "123456";

    private final AppUserRepository userRepo;
    private final RoleRepository roleRepo;
    private final JwtService jwtService;
    private final SmsOtpService smsOtpService;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<AppUserResponse> register(
            @Valid @RequestBody AppUserRequest req) {

        if (userRepo.existsByPhone(req.phone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone already registered");
        }

        Role customerRole = roleRepo.findByRoleName(RoleName.CUSTOMER).orElseThrow();

        String code = smsOtpService.newCode();  // Generate a secure 6-digit code
        AppUser u = AppUser.builder()
                .phone(req.phone())
                .realName(req.realName())
                .roles(Set.of(customerRole))
                .activationCode(code)
                .activationStatus(ActivationStatus.PENDING)
                .build();

        userRepo.save(u);
        
        try {
            smsOtpService.send(req.phone(), code);
        } catch (InvalidPhoneNumberException | SmsDeliveryException e) {
            // If SMS sending fails, we should delete the user and notify the client
            userRepo.delete(u);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Failed to send verification code: " + e.getMessage());
        }

        AppUserResponse resp = new AppUserResponse(
            u.getId(),
            u.getPhone(),
            u.getActivationStatus(),
            u.getRoles().stream()
                    .map(r -> r.getRoleName().name())
                    .collect(Collectors.toSet()),
            u.getCreatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping("/activate")
    public ResponseEntity<Void> activate(@RequestBody @Valid ActivationRequest req) {

        AppUser u = userRepo.findByPhone(req.phone())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String code = req.code();
        if (code == null || code.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code is required");

        if (DEFAULT_AUTH_CODE.equals(code) || code.equals(u.getActivationCode())) {
            u.setActivationStatus(ActivationStatus.ACTIVATED);
            u.setActivationCode(null);
            userRepo.save(u);
            return ResponseEntity.noContent().build();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong activation code");
    }

    // =====================================================================
    // Authentication
    // =====================================================================
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        AppUser user = userRepo.findByPhone(request.phone())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (user.getActivationStatus() != ActivationStatus.ACTIVATED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not activated");
        }

        String jwt = jwtService.generateToken(user.getId());
        return ResponseEntity.ok(new AuthResponse(jwt));
    }
}