package com.hrmf.hrms_backend.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubMitDocumentForVerificationDto {

    @NotBlank(message = "Employee name is required")
    private String employeeName;

    @NotBlank(message = "Employee id is required")
    private String employeeId;

    @NotBlank(message = "ShareCode is required")
    private String shareCode;

    @NotBlank(message = "Document type is this")
    private String documentType;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

//    @NotBlank(message = "Document url is required")
//    private String documentUrl;

    @NotNull(message = "Document file is required")
    private MultipartFile documentFile;
}
