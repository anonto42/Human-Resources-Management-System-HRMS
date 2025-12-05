package com.hrmf.hrms_backend.dto.employer;

import jakarta.validation.constraints.*;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
public class AddEmployeeRequestDto {

    @NotBlank(message = "You must give a name of employee")
    private String name;

    @NotBlank(message = "You must give a role of an employee")
    private String employeeRole;

    @NotNull(message = "You must give the DateOfBirth")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;

    @NotBlank(message = "You must give the gender")
    private String gender;

    @NotBlank(message = "You must give the shift of the employee")
    private String shift;

    @NotBlank(message = "You must give the office time")
    private String officeTime;

    @NotBlank(message = "you must give the number")
    private String number;

    @NotBlank(message = "You must give the email")
    @Email(message = "Your give email should be a valid email")
    private String email;

    @NotBlank(message = "You must give hte password")
    private String password;
}
