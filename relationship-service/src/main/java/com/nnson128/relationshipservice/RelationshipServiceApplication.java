package com.nnson128.relationshipservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RelationshipServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(RelationshipServiceApplication.class, args);
	}
}
