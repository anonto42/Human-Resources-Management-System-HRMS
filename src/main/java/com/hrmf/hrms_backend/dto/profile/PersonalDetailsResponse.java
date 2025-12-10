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
public class PersonalDetailsResponse {
    public String id;
    private String about;
    private String employerCode;
    private String niNo;
    private String gender;
    private LocalDate dateOfBirth;
    private String maritalStatus;
}