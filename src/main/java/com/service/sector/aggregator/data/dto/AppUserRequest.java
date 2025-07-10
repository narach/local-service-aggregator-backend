package com.service.sector.aggregator.data.dto;

import jakarta.validation.constraints.*;

/**
 * Payload for POST /api/users registration.
 * <p>
 * Business rules:
 *   • Either <b>email</b> or <b>phone</b> must be supplied (or both).<br>
 *   • <b>password</b> must be at least 6 chars and include lowercase, uppercase, digit.
 */
public record AppUserRequest(
        @Email @Size(max = 255) String email,
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone must contain 7‑15 digits, optional leading +")
        String phone,
        @NotBlank String realName,
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$",
                message = "Password must be ≥6 chars and include lowercase, uppercase, number")
        String password
) {
    /** Bean‑validation cross‑field rule: at least one contact way present. */
    @AssertTrue(message = "Either email or phone must be provided")
    public boolean isContactProvided() {
        return (email != null && !email.isBlank()) ||
                (phone != null && !phone.isBlank());
    }
}
