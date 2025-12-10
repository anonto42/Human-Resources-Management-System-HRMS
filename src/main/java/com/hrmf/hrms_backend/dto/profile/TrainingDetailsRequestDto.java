package com.hrmf.hrms_backend.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TrainingDetailsRequestDto {

    @NotBlank(message = "Training name is required")
    private String trainingName;

    @NotBlank(message = "Certificate name is required")
    private String certificateName;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;
}
