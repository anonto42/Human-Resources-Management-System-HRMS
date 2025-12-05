package com.hrmf.hrms_backend.dto.superAdmin;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CreateSubAdminResponseDto {
    String name;
    String email;
    String id;
}
