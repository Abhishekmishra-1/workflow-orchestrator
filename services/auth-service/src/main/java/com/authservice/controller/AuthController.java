package com.authservice.controller;

import com.authservice.dto.*;
import com.authservice.entity.User;
import com.authservice.security.RateLimiterService;
import com.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RateLimiterService rateLimiterService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        
        // Rate limiting
        if (!rateLimiterService.isLoginAllowed(req.getUsername(), ipAddress)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded. Please try again later.");
        }
        
        LoginResponse resp = authService.login(req, getDeviceInfo(request), ipAddress);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest req) {
        User created = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("auth-service up");
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private String getDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent.substring(0, Math.min(500, userAgent.length())) : null;
    }
}
