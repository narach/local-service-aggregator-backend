package com.service.sector.aggregator.controllers;

import com.service.sector.aggregator.data.dto.AppUserRequest;
import com.service.sector.aggregator.data.dto.AppUserResponse;
import com.service.sector.aggregator.data.dto.PhoneRequest;
import com.service.sector.aggregator.data.dto.auth.LoginRequest;
import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.exceptions.InvalidPhoneNumberException;
import com.service.sector.aggregator.exceptions.SmsDeliveryException;
import com.service.sector.aggregator.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "Operations about application users")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {

    private final UserService userService;

    // ---------------------------------------------------------------------
    // Check Phone
    // ---------------------------------------------------------------------
    @PostMapping("/request-code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Phone is valid and SMS code is successfully sent"),
            @ApiResponse(responseCode = "400", description = "Phone number is not in valid format"),
            @ApiResponse(responseCode = "500", description = "SMS sending failure(Issue with AWS SNS"),
    })
    public ResponseEntity<String> requestCode(@Valid @RequestBody PhoneRequest req) {
        try {
            userService.sendCode(req.phone());
        } catch (InvalidPhoneNumberException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SmsDeliveryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // ---------------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------------
    @Operation(summary = "Register new user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "409", description = "Phone is already registered")
    })
    @PostMapping("/register")
    public ResponseEntity<AppUserResponse> register(@Valid @RequestBody AppUserRequest req) {
        AppUserResponse response = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ---------------------------------------------------------------------
    // Authentication
    // ---------------------------------------------------------------------
    @Operation(summary = "User login")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "409", description = "Provided auth code doesn't match expected"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/login")
    public ResponseEntity<AppUserResponse> login(@Valid @RequestBody LoginRequest request) {
        AppUserResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    // ---------------------------------------------------------------------
    // Receive user details
    // ---------------------------------------------------------------------
    @Operation(summary = "User details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<AppUserResponse> userDetails(@PathVariable Long userId) {
        AppUserResponse userDto = userService.getUserDetails(userId);
        return ResponseEntity.ok(userDto);
    }
}
