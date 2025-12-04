package com.authservice.config;

import com.authservice.security.PemUtils;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Configuration for JWT encoding and decoding using RSA keys.
 * Provides JwtEncoder (for signing) and JwtDecoder (for validation).
 */
@Configuration
public class JwtConfig {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public JwtConfig(
            PemUtils pemUtils,
            @Value("${jwt.private-key-file}") Resource privateKeyResource,
            @Value("${jwt.public-key-file}") Resource publicKeyResource
    ) throws Exception {
        this.privateKey = pemUtils.parsePrivateKey(privateKeyResource);
        this.publicKey = pemUtils.parsePublicKey(publicKeyResource);
    }

    /**
     * JWT Encoder using RSA private key for signing (RS256).
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    /**
     * JWT Decoder using RSA public key for validation (RS256).
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    /**
     * JWK Set for JWKS endpoint (if needed for OAuth2 resource servers).
     * Only exposes the public key - private key is kept internal.
     */
    @Bean
    public JWKSet jwkSet() {
        RSAKey publicJwk = new RSAKey.Builder(publicKey)
                .keyID("auth-service-key-1")
                .build();
        return new JWKSet(publicJwk);
    }
}

