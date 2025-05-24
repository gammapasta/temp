package com.team109.javara;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JavaraApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaraApplication.class, args);
	}

}
