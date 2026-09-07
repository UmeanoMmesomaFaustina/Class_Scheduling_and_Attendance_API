
package com.schedule.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication

public class SchedulingApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(SchedulingApiApplication.class, args);
		System.out.println("Scheduling API started successfully!");
	}
}
