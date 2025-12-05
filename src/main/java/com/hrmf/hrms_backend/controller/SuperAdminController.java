package com.hrmf.hrms_backend.controller;

import com.hrmf.hrms_backend.dto.superAdmin.CreateSubAdminRequestDto;
import com.hrmf.hrms_backend.dto.superAdmin.CreateSubAdminResponseDto;
import com.hrmf.hrms_backend.service.SuperAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    @PostMapping("/sub-admin")
    public ResponseEntity<?> createSubAdmin (
            @Valid
            @RequestBody CreateSubAdminRequestDto createSubAdminRequestDto
            ){

        CreateSubAdminResponseDto response = superAdminService.createSuperAdmin(createSubAdminRequestDto);

        return ResponseEntity
                .ok()
                .body(response);
    }

}
