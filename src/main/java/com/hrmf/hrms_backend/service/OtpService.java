package com.hrmf.hrms_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final PasswordEncoder passwordEncoder;

    @Value("${application.security.otp.expiration:300}")
    private int otpExpirationSeconds;

    @Value("${application.security.otp.length:6}")
    private int otpLength;

    // In-memory store for OTPs (use Redis)
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();

    public String generateOtp(String email) {
        // Generate random 6-digit OTP
        String otp = String.format("%06d", (int) (Math.random() * 1000000));

        OtpData otpData = new OtpData(
                otp,
                LocalDateTime.now().plusSeconds(otpExpirationSeconds)
        );

        otpStore.put(email.toLowerCase(), otpData);
        log.info("Generated OTP for {}: {}", email, otp);

        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        String emailKey = email.toLowerCase();
        OtpData otpData = otpStore.get(emailKey);

        if (otpData == null) {
            log.warn("No OTP found for email: {}", email);
            return false;
        }

        // Check if OTP is expired
        if (otpData.expiresAt.isBefore(LocalDateTime.now())) {
            otpStore.remove(emailKey);
            log.warn("OTP expired for email: {}", email);
            return false;
        }

        // Verify OTP
        boolean isValid = otpData.otp.equals(otp);

        if (isValid) {
            otpStore.remove(emailKey);
            log.info("OTP verified successfully for email: {}", email);
        } else {
            log.warn("Invalid OTP for email: {}", email);
        }

        return isValid;
    }

    public void removeOtp(String email) {
        otpStore.remove(email.toLowerCase());
        log.info("Removed OTP for email: {}", email);
    }

    public boolean hasValidOtp(String email) {
        String emailKey = email.toLowerCase();
        OtpData otpData = otpStore.get(emailKey);

        if (otpData == null) {
            return false;
        }

        // Check if OTP is expired
        if (otpData.expiresAt.isBefore(LocalDateTime.now())) {
            otpStore.remove(emailKey);
            return false;
        }

        return true;
    }

    public long getOtpRemainingTime(String email) {
        String emailKey = email.toLowerCase();
        OtpData otpData = otpStore.get(emailKey);

        if (otpData == null) {
            return 0;
        }

        return Math.max(0, java.time.Duration.between(LocalDateTime.now(), otpData.expiresAt).getSeconds());
    }

    public void generateToken(String otp, String email) {

        // Create token by encode otp
        String token = passwordEncoder.encode(otp);

        tokenStore.put(email.toLowerCase(), token);
        log.info("Generated token for {}: {}", email, token);

    }

    public boolean verifyToken(String email, String otp) {

        String value = tokenStore.get(email.toLowerCase());

        if (value == null) {
            log.warn("No token found for email: {}", email);
            return false;
        }

        // Verify token
        boolean isValid = passwordEncoder.matches(otp,value);

        if (isValid) {
            tokenStore.remove(email.toLowerCase());
            log.info("Token verified successfully for email: {}", email);
        } else {
            log.warn("Invalid token for email: {}", email);
        }

        return isValid;
    }

    private static class OtpData {
        private final String otp;
        private final LocalDateTime expiresAt;

        OtpData(String otp, LocalDateTime expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }
    }
}