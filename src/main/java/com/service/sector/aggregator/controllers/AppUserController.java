package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.dto.AppUserRequest;
import com.service.sector.aggregator.data.dto.AppUserResponse;
import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

/**
 * REST‑контроллер для регистрации нового {@link AppUser}.
 */
@Tag(name = "Users", description = "Operations about application users")
@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserRepository userRepository;

    public AppUserController(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * POST /api/users – регистрация пользователя.
     */
    @Operation(summary = "Register new user", description = "Creates a new AppUser with unique e‑mail")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User successfully created",
                    content = @Content(schema = @Schema(implementation = AppUserResponse.class))),
            @ApiResponse(responseCode = "409", description = "E‑mail already registered", content = @Content)
    })
    @PostMapping
    public ResponseEntity<AppUserResponse> register(
            @RequestBody(description = "User registration payload", required = true)
            @Valid @org.springframework.web.bind.annotation.RequestBody AppUserRequest request)
    {

        // Проверяем уникальность email (400/409 – на ваше усмотрение)
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        // Маппинг DTO → Entity
        AppUser user = new AppUser();
        user.setEmail(request.email());
        user.setRealName(request.realName());
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        // Сохраняем
        AppUser saved = userRepository.save(user);

        // Маппинг Entity → Response DTO
        AppUserResponse response = new AppUserResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getRealName(),
                saved.getCreatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}

