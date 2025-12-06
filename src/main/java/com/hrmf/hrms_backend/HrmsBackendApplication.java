package com.hrmf.hrms_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class HrmsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrmsBackendApplication.class, args);
	}

}
