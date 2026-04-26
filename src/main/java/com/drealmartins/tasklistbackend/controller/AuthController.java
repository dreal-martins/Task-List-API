package com.drealmartins.tasklistbackend.controller;

import com.drealmartins.tasklistbackend.dto.*;
import com.drealmartins.tasklistbackend.entity.OtpType;
import com.drealmartins.tasklistbackend.service.AuthService;
import com.drealmartins.tasklistbackend.service.OtpService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RateLimiter(name = "auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgottenPasswordRequest request
    ) {
        return ResponseEntity.ok(authService.forgottenPassword(request));
    }

    @PostMapping("/reset-password/otp")
    public ResponseEntity<Map<String, String>> resetPasswordWithOtp(
            @Valid @RequestBody ResetPasswordWithOtpRequest request
    ) {
        return ResponseEntity.ok(authService.resetPasswordWithOtp(request));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(
            @Valid @RequestBody OtpRequest request
    ) {
        return ResponseEntity.ok(authService.resendOtp(request));
    }
}