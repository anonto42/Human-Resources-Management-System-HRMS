package com.hrmf.hrms_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignInRequestDto {

    @NotBlank(message = "You must give the email!")
    @Email(message = "You must give a valid email!")
    private String email;

    @NotBlank(message = "You must give the password!")
    @Min(value = 6,message = "Password must be atlas 6 characters!")
    private String password;
}
