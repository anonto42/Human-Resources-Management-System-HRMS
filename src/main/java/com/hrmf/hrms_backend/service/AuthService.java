package com.hrmf.hrms_backend.service;

import com.hrmf.hrms_backend.dto.ApiResponseDto;
import com.hrmf.hrms_backend.dto.auth.*;
import com.hrmf.hrms_backend.entity.CustomUserDetails;
import com.hrmf.hrms_backend.entity.User;
import com.hrmf.hrms_backend.enums.UserStatus;
import com.hrmf.hrms_backend.exception.BusinessException;
import com.hrmf.hrms_backend.repository.EmployeeRepository;
import com.hrmf.hrms_backend.repository.PersonalDetailsRepository;
import com.hrmf.hrms_backend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final PersonalDetailsRepository personalDetailsRepository;
    private final SecurityUtil securityUtil;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;

    public SignInResponseDto signIn(SignInRequestDto data) {
        log.info("Login attempt for email: {}", data.getEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(data.getEmail().toLowerCase().trim(), data.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // Check user status
        if (user.getStatus().equals(UserStatus.DELETED) || user.getStatus().equals(UserStatus.BLOCKED)) {
            throw BusinessException.forbidden("User is { " + user.getStatus().toString() + " }!");
        }

        // Generate tokens
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Update refresh token in database
        userService.updateRefreshToken(user.getId(), refreshToken);

        // Prepare user info
        Map<String, String> responseUser = new HashMap<>();
        responseUser.put("id", user.getId().toString());
        responseUser.put("email", user.getEmail());
        responseUser.put("name", user.getName());
        responseUser.put("role", user.getRole().name());
        responseUser.put("status", user.getStatus().name());

        log.info("Login successful for user: {}", user.getEmail());

        return SignInResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(responseUser)
                .build();
    }

    // Forgot password - send OTP to email
    @Transactional
    public ApiResponseDto<Void> forgotPassword(ForgotPasswordRequestDto request) {
        log.info("Forgot password request for email: {}", request.getEmail());

        String email = request.getEmail().toLowerCase().trim();

        // Check if user exists
        User user = userService.findByEmail(email);

        // Check user status
        if (user.getStatus().equals(UserStatus.DELETED) || user.getStatus().equals(UserStatus.BLOCKED)) {
            throw BusinessException.forbidden("User account is " + user.getStatus().toString().toLowerCase());
        }

        // Generate OTP
        String otp = otpService.generateOtp(email);

        // Send OTP via email (implement this)
        emailService.sendPasswordResetOtpEmail(user.getEmail(), user.getName(), otp);

        log.info("OTP sent to {} for password reset", email);

        return ApiResponseDto.<Void>success(
                "Password reset OTP has been sent to your email. Please check your inbox."
        );
    }

    // Verify OTP for password reset
    public ApiResponseDto<Void> verifyOtp(VerifyOtpRequestDto request) {
        log.info("Verify OTP request for email: {}", request.getEmail());

        String email = request.getEmail().toLowerCase().trim();
        String otp = request.getOtp();

        // Check if user exists
        userService.findByEmail(email);

        // Verify OTP
        boolean isValid = otpService.verifyOtp(email, otp);

        if (!isValid) {
            throw BusinessException.badRequest("Invalid or expired OTP");
        }

        otpService.generateToken(otp, email);

        return ApiResponseDto.<Void>success("OTP verified successfully");
    }

    // Reset password with OTP verification
    @Transactional
    public ApiResponseDto<Void> resetPassword(ResetPasswordRequestDto request) {
        log.info("Reset password request for email: {}", request.getEmail());

        String email = request.getEmail().toLowerCase().trim();
        String otp = request.getOtp();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            throw BusinessException.badRequest("Passwords do not match");
        }

        // Validate password strength
        if (newPassword.length() < 6) {
            throw BusinessException.badRequest("Password must be at least 6 characters");
        }

        // Check if user exists
        User user = userService.findByEmail(email);

        // Check user status
        if (user.getStatus().equals(UserStatus.DELETED) || user.getStatus().equals(UserStatus.BLOCKED)) {
            throw BusinessException.forbidden("User account is " + user.getStatus().toString().toLowerCase());
        }

        // Verify OTP
        boolean isValid = otpService.verifyToken(email, otp);

        if (!isValid) {
            throw BusinessException.badRequest("You are unAble to change the password!");
        }

        // Update password
        user.setHashPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userService.saveUser(user);

        // Invalidate all existing tokens
        userService.updateRefreshToken(user.getId(), null);

        // Send confirmation email
        emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getName());

        log.info("Password reset successful for user: {}", email);

        return ApiResponseDto.<Void>success("Password has been reset successfully");
    }

    // Change password for logged-in user
    @Transactional
    public ApiResponseDto<Void> changePassword(ChangePasswordRequestDto request) {

        log.info("This is the request ", request);

        User currentUser = securityUtil.getCurrentUserOrThrow();

        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            throw BusinessException.badRequest("New passwords do not match");
        }

        // Validate password strength
        if (newPassword.length() < 6) {
            throw BusinessException.badRequest("New password must be at least 6 characters");
        }

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, currentUser.getHashPassword())) {
            throw BusinessException.badRequest("Current password is incorrect");
        }

        // Check if new password is same as current password
        if (passwordEncoder.matches(newPassword, currentUser.getHashPassword())) {
            throw BusinessException.badRequest("New password cannot be the same as current password");
        }

        // Update password
        currentUser.setHashPassword(passwordEncoder.encode(newPassword));
        currentUser.setUpdatedAt(LocalDateTime.now());
        userService.saveUser(currentUser);

        // Invalidate all existing tokens
        userService.updateRefreshToken(currentUser.getId(), null);

        // Send confirmation email
        emailService.sendPasswordChangeConfirmationEmail(currentUser.getEmail(), currentUser.getName());

        log.info("Password changed successfully for user: {}", currentUser.getEmail());

        return ApiResponseDto.<Void>success("Password has been changed successfully");
    }

    // Logout invalidate refresh token
    @Transactional
    public ApiResponseDto<Void> logout() {
        User currentUser = securityUtil.getCurrentUserOrThrow();

        // Check if already logged out
        if (currentUser.getRefreshToken() == null) {
            log.warn("User already logged out: {}", currentUser.getEmail());
            return ApiResponseDto.<Void>success("Already logged out");
        }

        // Clear refresh token
        userService.updateRefreshToken(currentUser.getId(), null);

        // Clear security context
        SecurityContextHolder.clearContext();

        log.info("User logged out: {}", currentUser.getEmail());

        return ApiResponseDto.<Void>success("Logged out successfully");
    }

    // Refresh access token using refresh token
    public SignInResponseDto refreshToken(String refreshToken) {
        log.info("Refresh token request");

        if (!StringUtils.hasText(refreshToken)) {
            throw BusinessException.badRequest("Refresh token is required");
        }

        // Find user by refresh token
        User user = userService.findByRefreshToken(refreshToken)
                .orElseThrow(() -> BusinessException.unauthorized("Invalid refresh token"));

        // Check if refresh token matches
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw BusinessException.unauthorized("Invalid refresh token");
        }

        // Check user status
        if (user.getStatus().equals(UserStatus.DELETED) || user.getStatus().equals(UserStatus.BLOCKED)) {
            throw BusinessException.forbidden("User account is " + user.getStatus().toString().toLowerCase());
        }

        // Generate new tokens
        CustomUserDetails userDetails = user.toUserDetails();
        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        // Update refresh token in database
        userService.updateRefreshToken(user.getId(), newRefreshToken);

        // Prepare user info
        Map<String, String> responseUser = new HashMap<>();
        responseUser.put("id", user.getId().toString());
        responseUser.put("email", user.getEmail());
        responseUser.put("name", user.getName());
        responseUser.put("role", user.getRole().name());
        responseUser.put("status", user.getStatus().name());

        log.info("Token refreshed for user: {}", user.getEmail());

        return SignInResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(responseUser)
                .build();
    }
}