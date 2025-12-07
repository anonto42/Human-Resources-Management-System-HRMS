package com.hrmf.hrms_backend.dto.user;

import com.hrmf.hrms_backend.enums.Gender;
import com.hrmf.hrms_backend.enums.MaritalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalDetailsDto {
    private String about;
    private String employerCode;
    private Gender gender;
    private LocalDate dateOfBirth;
    private MaritalStatus maritalStatus;
    private String mobile;
    private String emergencyContact;
}