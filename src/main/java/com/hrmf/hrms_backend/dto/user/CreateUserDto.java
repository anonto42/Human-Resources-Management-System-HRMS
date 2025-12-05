package com.hrmf.hrms_backend.dto.user;

import com.hrmf.hrms_backend.enums.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDto {
    private String name;
    private String email;
    private String password;
    private UserRole role;
}
