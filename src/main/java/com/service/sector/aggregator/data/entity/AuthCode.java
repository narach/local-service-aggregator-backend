package com.service.sector.aggregator.data.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Schema(description = "Phone numbers and sent sms codes")
@Entity
@Table(name = "auth_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"phone", "code"})
@ToString(of = {"id", "phone", "code"})
public class AuthCode {

    @Schema(description = "Unique ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Phone number")
    @Column(length = 17)
    private String phone;

    @Schema(description = "Generated SMS auth code")
    @Column(name = "sms_code", length = 6)
    private String code;
}

