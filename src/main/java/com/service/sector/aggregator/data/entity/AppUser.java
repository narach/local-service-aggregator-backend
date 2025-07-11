package com.service.sector.aggregator.data.entity;

import com.service.sector.aggregator.data.enums.ActivationStatus;
import com.service.sector.aggregator.data.enums.RoleName;
import com.service.sector.aggregator.data.enums.RoleRequestStatus;
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

    @Column
    private String password;

    @Column(name = "activation_code")          // null == activated (legacy users)
    private String activationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "activation_status", nullable = false, length = 10)
    @Builder.Default
    private ActivationStatus activationStatus = ActivationStatus.PENDING;

    @Builder.Default                                            // ← add
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder.Default                                            // ← add
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    /** Many-to-many through table app_user_role */
    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)                // EAGER because roles are tiny
    @JoinTable(
            name = "app_user_role",
            joinColumns        = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private java.util.Set<Role> roles = new java.util.HashSet<>();

    // NEW helper methods -------------------------------------------------------
    public boolean hasRole(RoleName rn) {
        return roles.stream().anyMatch(r -> r.getRoleName() == rn);
    }

    public void addRole(Role r) { roles.add(r); }

    @Enumerated(EnumType.STRING)
    @Column(name = "master_role_status", nullable = false, length = 20)
    @Builder.Default
    private RoleRequestStatus masterRoleStatus = RoleRequestStatus.NO;

    @Enumerated(EnumType.STRING)
    @Column(name = "landlord_role_status", nullable = false, length = 20)
    @Builder.Default
    private RoleRequestStatus landlordRoleStatus = RoleRequestStatus.NO;
}
