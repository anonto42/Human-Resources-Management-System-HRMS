package com.hrmf.hrms_backend.controller;

import com.hrmf.hrms_backend.dto.auth.SignInRequestDto;
import com.hrmf.hrms_backend.dto.auth.SignInResponseDto;
import com.hrmf.hrms_backend.dto.employer.AddEmployeeResponseDto;
import com.hrmf.hrms_backend.dto.user.CreateUserDto;
import com.hrmf.hrms_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login (
            @Valid
            @RequestBody SignInRequestDto signInRequestDto
        ){
        SignInResponseDto response = authService.signIn(signInRequestDto);
        return ResponseEntity
                .ok()
                .body(response);
    }

    @PostMapping("/create-employer")
    public ResponseEntity<?> createEmployer (
            @Valid
            @RequestBody CreateUserDto signInRequestDto
    ){
        AddEmployeeResponseDto response = authService.createEmployer(signInRequestDto);

        return ResponseEntity
                .ok()
                .body(response);
    }
}
