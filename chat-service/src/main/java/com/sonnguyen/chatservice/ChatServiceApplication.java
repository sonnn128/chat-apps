package com.sonnguyen.chatservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@SpringBootApplication
@EnableFeignClients
@EnableCassandraRepositories
public class ChatServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChatServiceApplication.class, args);
	}
}
