package com.hrmf.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDto {
    String accessToken;
    String refreshToken;
    UserInfoDto userInfo;
}
