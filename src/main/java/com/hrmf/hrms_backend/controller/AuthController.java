package com.hrmf.hrms_backend.controller;

import com.hrmf.hrms_backend.dto.ApiResponseDto;
import com.hrmf.hrms_backend.dto.auth.*;
import com.hrmf.hrms_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<?>> login(
            @Valid @RequestBody SignInRequestDto signInRequestDto) {

        SignInResponseDto response = authService.signIn(signInRequestDto);

        return ResponseEntity.ok(
                ApiResponseDto.success("Login successful", response)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseDto<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request) {

        ApiResponseDto<Void> response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponseDto<Void>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequestDto request) {

        ApiResponseDto<Void> response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDto<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request) {

        ApiResponseDto<Void> response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponseDto<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto request) {

        ApiResponseDto<Void> response = authService.changePassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout() {

        ApiResponseDto<Void> response = authService.logout();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponseDto<?>> refreshToken(
            @RequestParam String refreshToken) {

        SignInResponseDto response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(
                ApiResponseDto.success("Token refreshed successfully", response)
        );
    }
}