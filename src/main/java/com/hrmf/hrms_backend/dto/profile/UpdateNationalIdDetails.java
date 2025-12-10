package com.hrmf.hrms_backend.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateNationalIdDetails {

    @NotBlank(message = "You must give the national id")
    private String nationalIdNumber;

    @NotBlank(message = "You must give nationality!")
    private String nationality;

    @NotBlank(message = "You must give the name of the country residence!")
    private String countryOfResidence;

    @NotNull(message = "Give the issue date!")
    private LocalDate issueDate;

    @NotNull(message = "Give the expiry date!")
    private LocalDate expiryDate;

    @NotBlank(message = "Must give the document name!")
    private String documentName;

    @NotBlank(message = "You must give hte number!")
    private String documentRefNumber;

    @NotNull(message = "Give the document issue date!")
    private LocalDate otherDocumentIssueDate;

    @NotNull(message = "Give the document expiry date!")
    private LocalDate otherDocumentExpiryDate;
}
