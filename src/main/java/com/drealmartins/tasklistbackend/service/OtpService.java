package com.drealmartins.tasklistbackend.service;

import com.drealmartins.tasklistbackend.entity.OtpCode;
import com.drealmartins.tasklistbackend.entity.OtpType;
import com.drealmartins.tasklistbackend.entity.User;
import com.drealmartins.tasklistbackend.exception.InvalidTokenException;
import com.drealmartins.tasklistbackend.repository.OtpRepository;
import com.drealmartins.tasklistbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public void generateAndSendOtp(String email, OtpType type) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new InvalidTokenException("No account found with email: " + email));

        otpRepository.deleteByEmailAndType(email, type);

        String otp = generateOtp();

        OtpCode otpCode = OtpCode.builder()
                .code(otp)
                .email(email)
                .type(type)
                .expiryDate(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .verified(false)
                .createdAt(LocalDateTime.now())
                .attemptCount(0)
                .build();

        otpRepository.save(otpCode);

        try {
//            String userName = user.getUsername() != null ? user.getUsername() : "User";
            emailService.sendSimpleOtpEmail(email, otp);
            log.info("OTP generated and sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", email, e);
            throw new RuntimeException("Failed to send OTP email. Please try again.");
        }
    }

    @Transactional
    public boolean verifyOtp(String email, String code, OtpType type) {
        OtpCode otpCode = otpRepository
                .findByEmailAndCodeAndTypeAndVerifiedFalse(email, code, type)
                .orElseThrow(() -> new InvalidTokenException("Invalid OTP code"));

        if (otpCode.isMaxAttemptsReached()) {
            throw new InvalidTokenException("Maximum verification attempts exceeded. Please request a new OTP.");
        }

        if (otpCode.isExpired()) {
            throw new InvalidTokenException("OTP code has expired. Please request a new one.");
        }

        otpCode.incrementAttempts();
        otpRepository.save(otpCode);

        if (!otpCode.getCode().equals(code)) {
            int remainingAttempts = 5 - otpCode.getAttemptCount();
            throw new InvalidTokenException(
                    "Invalid OTP code. " + remainingAttempts + " attempts remaining."
            );
        }

        otpCode.setVerified(true);
        otpRepository.save(otpCode);

        log.info("OTP verified successfully for email: {}", email);
        return true;
    }

    @Transactional
    public void resetPasswordWithOtp(String email, String code, String newPassword) {
        verifyOtp(email, code, OtpType.PASSWORD_RESET);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpRepository.deleteByEmail(email);

        try {
            String userName = user.getUsername() != null ? user.getUsername() : "User";
            emailService.sendPasswordResetSuccessEmail(email, userName);
        } catch (Exception e) {
            log.error("Failed to send password reset success email", e);
        }

        log.info("Password reset successfully for email: {}", email);
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

}