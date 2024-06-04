package com.dart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DartApplication {

	public static void main(String[] args) {
		SpringApplication.run(DartApplication.class, args);
	}
}
