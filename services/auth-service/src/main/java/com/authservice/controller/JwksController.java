package com.authservice.controller;

import com.nimbusds.jose.jwk.JWKSet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for exposing JWKS (JSON Web Key Set) endpoint.
 * Used by OAuth2 resource servers to validate JWT tokens.
 */
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class JwksController {

    private final JWKSet jwkSet;

    /**
     * Expose JWKS endpoint for public key distribution.
     * GET /oauth2/jwks
     */
    @GetMapping(value = "/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JWKSet> jwks() {
        return ResponseEntity.ok(jwkSet);
    }
}

