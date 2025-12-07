package com.hrmf.hrms_backend.dto.superAdmin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubAdminDto {
    private String id;
    private String name;
    private String email;
    private String contactNumber;
    private Integer totalEmployer;
    private String status;
}
