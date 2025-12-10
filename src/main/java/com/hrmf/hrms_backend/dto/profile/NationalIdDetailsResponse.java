package com.hrmf.hrms_backend.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NationalIdDetailsResponse {
    private String id;
    private String nationalIdNumber;
    private String nationality;
    private String countryOfResidence;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentName;
    private String documentRefNumber;
    private LocalDate otherDocumentIssueDate;
    private LocalDate otherDocumentExpiryDate;
}