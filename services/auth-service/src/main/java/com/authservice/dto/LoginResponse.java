package com.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token; // Access token (for backward compatibility)
    private String accessToken; // Access token
    private String refreshToken; // Refresh token
    private Long accessTokenExpiresIn; // milliseconds
    private Long refreshTokenExpiresIn; // milliseconds
}
