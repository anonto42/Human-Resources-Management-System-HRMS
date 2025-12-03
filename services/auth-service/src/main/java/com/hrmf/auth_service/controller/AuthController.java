package com.hrmf.auth_service.controller;

import com.hrmf.auth_service.dto.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login (){

        return ResponseEntity.ok().body({});
    }
}
