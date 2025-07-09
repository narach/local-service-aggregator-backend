package com.service.sector.aggregator.data.dto.auth;

import jakarta.validation.constraints.*;

public record AuthRequest(
        @Size(max = 255) String email,
        @Pattern(regexp = "^\\+?[0-9]{7,15}$") String phone,
        @NotBlank String password
) {
    @AssertTrue(message = "Provide email or phone")
    public boolean hasContact() {
        return (email != null && !email.isBlank()) ||
                (phone != null && !phone.isBlank());
    }
}
