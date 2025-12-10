package com.hrmf.hrms_backend.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityDocumentResponse {
    private String id;
    private String documentType;
    private String documentNumber;
    private String nationality;
    private String country;
    private String issuedBy;
    private String issueDate;
    private String expiryDate;
    private String shareCode;
    private String immigrationStatus;
    private String countryResidency;
    private Boolean isCurrent;
    private List<String> documentUrls;
    private Integer documentCount;
}