package com.hrmf.hrms_backend.dto.paySlip;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class UpdatePaySlipe {
    private String employeeName;
    private String employeeId;
    private String jobCategory;
    private LocalDate transactionDate;
    private String amount;
    private String description;
    private MultipartFile imageFile;
}