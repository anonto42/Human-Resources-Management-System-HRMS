package com.hrmf.hrms_backend.controller;

import com.hrmf.hrms_backend.service.SubAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sub-admin")
@RequiredArgsConstructor
public class SubAdminController {

    private final SubAdminService subAdminService;

}
