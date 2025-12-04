package com.authservice.service;

import com.authservice.dto.*;
import com.authservice.entity.User;
import com.authservice.repository.UserRepository;
import com.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        // Issue both access and refresh tokens
        JwtIssue jwtIssue = jwtService.issueTokens(user.getUsername());
        
        return LoginResponse.builder()
                .token(jwtIssue.getAccessToken()) // For backward compatibility
                .accessToken(jwtIssue.getAccessToken())
                .refreshToken(jwtIssue.getRefreshToken())
                .accessTokenExpiresIn(jwtIssue.getAccessTokenExpiresIn())
                .refreshTokenExpiresIn(jwtIssue.getRefreshTokenExpiresIn())
                .build();
    }

    public User register(RegisterRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        User u = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole() == null ? "USER" : req.getRole().toUpperCase())
                .build();
        return userRepository.save(u);
    }
}
