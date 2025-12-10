package com.hrmf.hrms_backend.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class UpdatePassportDetailsDto {

    @NotBlank(message = "Passport number is required")
    private String passportNumber;

    @NotBlank(message = "Nationality is required")
    private String nationality;

    private String issueBy;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Expiry date is required")
    private LocalDate expDate;

    private MultipartFile[] documents;

    @NotNull(message = "Is current field is required")
    private Boolean isCurrent;
}