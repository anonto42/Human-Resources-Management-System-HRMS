package com.hrmf.hrms_backend.dto.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmergencyDetailsDto {

    @NotBlank(message = "You must give the name")
    public String name;

    @NotBlank(message = "You must give the relation with person")
    public String relation;

    @NotBlank(message = "You must give the details")
    public String giveDetails;

    @NotBlank(message = "Email is required")
    @Email(message = "Must give a valid email")
    public String email;

    @NotBlank(message = "Emergency contact number is require")
    public String emergencyContact;

    @NotBlank(message = "Address is required")
    public String address;

    @NotBlank(message = "Title of license is required")
    public String titleOfCertifiedLicense;

    @NotBlank(message = "License number is required")
    public String licenseNumber;

    @NotNull(message = "Issue date is required")
    public String issueDate;

    @NotNull(message = "Expiry date is required")
    public String expiryDate;
}
