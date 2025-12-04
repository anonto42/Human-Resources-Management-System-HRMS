package com.hrmf.hrmf_project.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@Getter
@Setter
@RequestScope
public class JwtContextHolder {
    private UUID currentUserId;
    private String currentUserEmail;
}