package com.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_token_hash", columnList = "token_hash"),
    @Index(name = "idx_expires_at", columnList = "expires_at"),
    @Index(name = "idx_session_id", columnList = "session_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    @Column(nullable = false, unique = true, length = 36)
    @Builder.Default
    private String sessionId = UUID.randomUUID().toString();

    @Column(length = 500)
    private String deviceInfo;

    @Column(length = 45)
    private String ipAddress;

    @Column
    private Instant lastUsedAt;
}

