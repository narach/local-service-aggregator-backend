package com.service.sector.aggregator.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * User entity.
 *
 * Constraints:
 *  • Either <b>email</b> or <b>phone</b> must be present (checked at service/controller level).
 *  • Password must be at least 6 chars and contain at least one lower‑case letter, one upper‑case letter and one digit.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "email", "phone", "realName"})
@Entity
@Table(name = "app_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uc_user_phone", columnNames = "phone")
        })
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Column(length = 255)
    private String email;          // optional but unique

    /** E.164 or local digits; simple pattern placeholder. */
    @Pattern(regexp = "^\\+?[0-9]{7,15}$")
    @Column(length = 20)
    private String phone;          // optional but unique

    @NotBlank
    @Column(name = "real_name", nullable = false, length = 255)
    private String realName;

    /**
     * Stored as hash (BCrypt, etc.).
     * Regex enforces ≥6 chars, 1 lowercase, 1 uppercase, 1 digit.
     */
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$",
            message = "Password must be ≥6 chars and include lowercase, uppercase, number")
    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
