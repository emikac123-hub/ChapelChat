package com.erikmikac.ChapelChat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ChapelChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChapelChatApplication.class, args);
	}
}
