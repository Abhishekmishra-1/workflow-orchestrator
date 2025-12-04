package com.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private String sessionId;
    private Instant issuedAt;
    private Instant lastUsedAt;
    private Instant expiresAt;
    private String deviceInfo;
    private String ipAddress;
    private Boolean revoked;
}

