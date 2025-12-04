package com.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for token revocation endpoint.
 * Either accessToken or refreshToken (or both) can be provided.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevokeRequest {
    private String accessToken;
    private String refreshToken;
}

