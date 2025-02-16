package com.greedy.mokkoji;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MokkojiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MokkojiApplication.class, args);
	}

}
