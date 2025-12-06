package com.hrmf.hrms_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    public void sendPasswordResetOtpEmail(String email, String name, String otp) {
        // TODO: Implement email sending
        // Use JavaMailSender or external service like SendGrid, AWS SES
        log.info("Sending password reset OTP to {}: {}", email, otp);

        // Example email content
        String subject = "Password Reset OTP - HRMS";
        String body = String.format("""
                Hello %s,
                
                Your password reset OTP is: %s
                
                This OTP will expire in 5 minutes.
                
                If you didn't request this, please ignore this email.
                
                Regards,
                HRMS Team
                """, name, otp);

        log.info("Email subject: {}", subject);
        log.info("Email body: {}", body);
    }

    public void sendPasswordResetConfirmationEmail(String email, String name) {
        log.info("Sending password reset confirmation to: {}", email);
    }

    public void sendPasswordChangeConfirmationEmail(String email, String name) {
        log.info("Sending password change confirmation to: {}", email);
    }
}