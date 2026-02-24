package com.drealmartins.tasklistbackend.service;

import com.drealmartins.tasklistbackend.dto.*;
import com.drealmartins.tasklistbackend.entity.OtpType;
import com.drealmartins.tasklistbackend.entity.Role;
import com.drealmartins.tasklistbackend.entity.User;
import com.drealmartins.tasklistbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;


    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);

        return new AuthResponse(jwtToken, user.getUsername(), user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var jwtToken = jwtService.generateToken(user);

        return new AuthResponse(jwtToken, user.getUsername(), user.getEmail());
    }

    public Map<String, String> forgottenPassword(ForgottenPasswordRequest request) {
        otpService.generateAndSendOtp(request.email(), OtpType.PASSWORD_RESET);

        return Map.of(
                "message", "A 6-digit OTP has been sent to your email",
                "email", request.email(),
                "expiryMinutes", "10"
        );
    }

    public Map<String, String> resetPasswordWithOtp(ResetPasswordWithOtpRequest request) {
        otpService.resetPasswordWithOtp(
                request.email(),
                request.code(),
                request.newPassword()
        );

        return Map.of(
                "message", "Password has been reset successfully"
        );
    }

    public Map<String,String> resendOtp(OtpRequest request){
        otpService.generateAndSendOtp(request.email(), OtpType.PASSWORD_RESET);

        return Map.of(
                "message", "A new OTP has been sent to your email",
                "email", request.email()
        );
    }
}
