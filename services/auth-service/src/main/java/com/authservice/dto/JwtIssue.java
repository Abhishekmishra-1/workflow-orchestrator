package com.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing access token, refresh token, and expiry information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtIssue {
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn; // milliseconds until expiry
    private Long refreshTokenExpiresIn; // milliseconds until expiry
}

