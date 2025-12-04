package com.authservice.repository;

import com.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find a refresh token by its hashed value.
     *
     * @param tokenHash the SHA-256 hash of the token
     * @return Optional RefreshToken
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Find a refresh token by session ID.
     *
     * @param sessionId the session ID
     * @return Optional RefreshToken
     */
    Optional<RefreshToken> findBySessionId(String sessionId);

    /**
     * Find all refresh tokens for a specific user.
     *
     * @param userId the user ID
     * @return List of RefreshToken
     */
    List<RefreshToken> findByUserId(Long userId);

    /**
     * Delete all refresh tokens for a specific user.
     * Useful for logout or token rotation.
     *
     * @param userId the user ID
     */
    void deleteByUserId(Long userId);
}

