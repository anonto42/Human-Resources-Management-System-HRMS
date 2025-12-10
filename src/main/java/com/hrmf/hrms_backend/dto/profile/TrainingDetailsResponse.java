package com.hrmf.hrms_backend.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingDetailsResponse {
    private String id;
    private String trainingName;
    private String certificateName;
    private String startDate;
    private String endDate;
    private String expiryDate;
}