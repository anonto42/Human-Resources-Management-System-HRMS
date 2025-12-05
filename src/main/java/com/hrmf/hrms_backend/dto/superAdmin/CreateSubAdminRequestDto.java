package com.hrmf.hrms_backend.dto.superAdmin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateSubAdminRequestDto {

    @NotBlank(message = "Give the name of sub-admin")
    private String name;

    @NotBlank(message = "You must give the email")
    @Email(message = "Email should be a valid email")
    private String email;

    @NotBlank(message = "You must give a password for the sub-admin")
    private String password;

    @NotBlank(message = "You must give the address")
    private String address;
}
