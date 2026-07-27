package com.alness.lifemaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LifeMasterApplication {

	public static void main(String[] args) {
		SpringApplication.run(LifeMasterApplication.class, args);
	}

}
