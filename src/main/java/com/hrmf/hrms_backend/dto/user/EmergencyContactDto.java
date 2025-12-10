package com.hrmf.hrms_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactDto {
    private UUID id;
    private String name;
    private String relationship;
    private String contactNumber;
    private String email;
    private String address;
    private String additionalDetails;
}