package com.drealmartins.tasklistbackend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Task List App}")
    private String appName;

    @Value("${app.support.email:support@tasklistapp.com}")
    private String supportEmail;

    public void sendOtpEmail(String to, String otp, String userName) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("otp", otp);
            context.setVariable("appName", appName);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("expiryMinutes", 10);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("otp-email", context);

            sendHtmlEmail(to, "Your Password Reset Code - " + appName, htmlContent);

            log.info("OTP email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", to, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    public void sendPasswordResetSuccessEmail(String to, String userName) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("appName", appName);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("resetTime", LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")
            ));
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("password-reset-success", context);

            sendHtmlEmail(to, "Password Reset Successful - " + appName, htmlContent);

            log.info("Password reset success email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset success email to: {}", to, e);
        }
    }

    public void sendWelcomeEmail(String to, String userName) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("appName", appName);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("welcome-email", context);

            sendHtmlEmail(to, "Welcome to " + appName + "!", htmlContent);

            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendSimpleOtpEmail(String to, String otp) {
        try {
            String subject = "Your Password Reset Code - " + appName;
            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #4a5568;">Password Reset Request</h2>
                        <p>Your OTP code is:</p>
                        <div style="background-color: #f7fafc; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0;">
                            <h1 style="color: #2d3748; letter-spacing: 5px; margin: 0;">%s</h1>
                        </div>
                        <p>This code will expire in 10 minutes.</p>
                        <p>If you didn't request this, please ignore this email.</p>
                        <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;">
                        <p style="color: #718096; font-size: 12px;">© %d %s. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """, otp, LocalDateTime.now().getYear(), appName);

            sendHtmlEmail(to, subject, htmlContent);
            log.info("Simple OTP email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple OTP email to: {}", to, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}