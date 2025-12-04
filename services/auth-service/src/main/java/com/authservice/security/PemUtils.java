package com.authservice.security;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Utility class for parsing PEM-encoded RSA keys.
 * Supports both private and public keys from PEM files.
 */
@Component
public class PemUtils {

    /**
     * Parse a PEM-encoded RSA private key from a Spring Resource.
     *
     * @param resource Spring Resource pointing to the PEM file
     * @return RSAPrivateKey instance
     * @throws IOException if the resource cannot be read or parsed
     */
    public RSAPrivateKey parsePrivateKey(Resource resource) throws IOException {
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            PEMParser pemParser = new PEMParser(reader);
            Object object = pemParser.readObject();
            pemParser.close();

            if (object instanceof PrivateKeyInfo) {
                PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) object;
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                return (RSAPrivateKey) converter.getPrivateKey(privateKeyInfo);
            } else if (object instanceof org.bouncycastle.openssl.PEMKeyPair) {
                org.bouncycastle.openssl.PEMKeyPair keyPair = (org.bouncycastle.openssl.PEMKeyPair) object;
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                return (RSAPrivateKey) converter.getPrivateKey(keyPair.getPrivateKeyInfo());
            } else {
                throw new IllegalArgumentException("Unsupported PEM object type: " + object.getClass());
            }
        }
    }

    /**
     * Parse a PEM-encoded RSA public key from a Spring Resource.
     *
     * @param resource Spring Resource pointing to the PEM file
     * @return RSAPublicKey instance
     * @throws IOException if the resource cannot be read or parsed
     */
    public RSAPublicKey parsePublicKey(Resource resource) throws IOException {
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            PEMParser pemParser = new PEMParser(reader);
            Object object = pemParser.readObject();
            pemParser.close();

            if (object instanceof SubjectPublicKeyInfo) {
                SubjectPublicKeyInfo publicKeyInfo = (SubjectPublicKeyInfo) object;
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                return (RSAPublicKey) converter.getPublicKey(publicKeyInfo);
            } else if (object instanceof org.bouncycastle.openssl.PEMKeyPair) {
                org.bouncycastle.openssl.PEMKeyPair keyPair = (org.bouncycastle.openssl.PEMKeyPair) object;
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                return (RSAPublicKey) converter.getPublicKey(keyPair.getPublicKeyInfo());
            } else {
                throw new IllegalArgumentException("Unsupported PEM object type: " + object.getClass());
            }
        }
    }
}

