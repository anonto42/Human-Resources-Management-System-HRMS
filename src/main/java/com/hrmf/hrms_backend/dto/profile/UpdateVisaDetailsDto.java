package com.hrmf.hrms_backend.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class UpdateVisaDetailsDto {

    @NotBlank(message = "Nationality is required")
    private String nationality;

    @NotBlank(message = "shareCode is required")
    private String shareCode;

    @NotBlank(message = "Immigration Status is required")
    private String immigrationStatus;

    @NotBlank(message = "Country Residency Status is required")
    private String countryResidency;

    private String issueBy;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Expiry date is required")
    private LocalDate expDate;

    private MultipartFile[] documents;

    @NotNull(message = "Is current visa field is required")
    private Boolean isCurrentVisa;
}
