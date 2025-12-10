package com.hrmf.hrms_backend.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyDetailsResponse {
    public String id;
    public String name;
    public String relation;
    public String giveDetails;
    public String email;
    public String emergencyContact;
    public String address;
    public String titleOfCertifiedLicense;
    public String licenseNumber;
    public String issueDate;
    public String expiryDate;
}