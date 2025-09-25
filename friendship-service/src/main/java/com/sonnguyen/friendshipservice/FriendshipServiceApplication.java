package com.sonnguyen.friendshipservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FriendshipServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(FriendshipServiceApplication.class, args);
	}
}
