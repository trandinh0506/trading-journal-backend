package com.trader.journal_backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class JournalBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(JournalBackendApplication.class, args);
	}

	@PostConstruct
	public void init() {
		// Set the default timezone to UTC
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

}
