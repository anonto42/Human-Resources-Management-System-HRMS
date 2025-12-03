package com.hrmf.auth_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final WebClient webClient;

    private String getUserInfo(String userId) {
        return  webClient.get()
                .uri()
    }
}
