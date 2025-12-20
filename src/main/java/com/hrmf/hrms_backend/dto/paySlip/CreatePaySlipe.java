package com.hrmf.hrms_backend.dto.paySlip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class CreatePaySlipe {

    @NotBlank(message = "You must give the name of employee name")
    private String employeeName;

    @NotBlank(message = "You must give the id of the User")
    private String userId;

    @NotBlank(message = "You must give the job category")
    private String jobCategory;

    @NotNull(message = "You must give the Transactions date")
    private LocalDate transactionDate;

    @NotBlank(message = "You must give the amount")
    private String amount;

    @NotBlank(message = "You must give the description")
    private String description;

    @NotNull(message = "image is require")
    private MultipartFile image;
}