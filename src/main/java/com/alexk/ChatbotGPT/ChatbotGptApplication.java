package com.alexk.ChatbotGPT;

import com.alexk.ChatbotGPT.Service.StoredChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ChatbotGptApplication {

	@Autowired
	StoredChatService storedChatService;

	public static void main(String[] args) {
		SpringApplication.run(ChatbotGptApplication.class, args);
	}
}
