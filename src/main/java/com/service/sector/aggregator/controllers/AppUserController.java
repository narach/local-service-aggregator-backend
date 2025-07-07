package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.dto.AppUserRequest;
import com.service.sector.aggregator.data.dto.AppUserResponse;
import com.service.sector.aggregator.data.dto.auth.AuthRequest;
import com.service.sector.aggregator.data.dto.auth.AuthResponse;
import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import com.service.sector.aggregator.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

/**
 * REST controller for user registration.
 */
@Tag(name = "Users", description = "Operations about application users")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Operation(summary = "Register new user", description = "Creates a new AppUser with unique e‑mail or phone")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User successfully created",
                    content = @Content(schema = @Schema(implementation = AppUserResponse.class))),
            @ApiResponse(responseCode = "409", description = "Contact already registered", content = @Content)
    })
    @PostMapping
    public ResponseEntity<AppUserResponse> register(
            @RequestBody(description = "User registration payload", required = true)
            @Valid @org.springframework.web.bind.annotation.RequestBody AppUserRequest request) {

        boolean hasEmail = StringUtils.hasText(request.email());
        boolean hasPhone = StringUtils.hasText(request.phone());

        if (hasEmail && userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        if (hasPhone && userRepository.existsByPhone(request.phone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone already registered");
        }

        AppUser user = AppUser.builder()
                .email(hasEmail ? request.email() : null)
                .phone(hasPhone ? request.phone() : null)
                .realName(request.realName())
                .password(passwordEncoder.encode(request.password()))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        AppUser saved = userRepository.save(user);

        AppUserResponse response = new AppUserResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getPhone(),
                saved.getRealName(),
                saved.getCreatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =====================================================================
    // Authentication
    // =====================================================================
    @Operation(summary = "User login", description = "Authenticate by email *or* phone and password; returns JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody(description = "Login payload", required = true)
            @Valid @org.springframework.web.bind.annotation.RequestBody AuthRequest req) {

        AppUser user;
        if (StringUtils.hasText(req.email())) {
            user = userRepository.findByEmail(req.email())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        } else {
            user = userRepository.findByPhone(req.phone())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        }

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        String subject = user.getEmail() != null ? user.getEmail() : user.getPhone();
        String token = jwtService.generateToken(user.getId(), subject);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
