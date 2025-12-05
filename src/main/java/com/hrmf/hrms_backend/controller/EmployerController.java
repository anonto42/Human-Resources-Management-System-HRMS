package com.hrmf.hrms_backend.controller;

import com.hrmf.hrms_backend.dto.employer.AddEmployeeRequestDto;
import com.hrmf.hrms_backend.dto.employer.AddEmployeeResponseDto;
import com.hrmf.hrms_backend.service.EmployerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employer")
@RequiredArgsConstructor
public class EmployerController {

    private final EmployerService employerService;

    @PostMapping("/add-employee")
    public ResponseEntity<?> createEmployee (
            @Valid
            @RequestBody AddEmployeeRequestDto addEmployeeRequestDto
            ){

        AddEmployeeResponseDto addEmployeeResponseDto = employerService.addEmployee(addEmployeeRequestDto);

        return ResponseEntity
                .ok()
                .body(addEmployeeResponseDto);
    }
}
