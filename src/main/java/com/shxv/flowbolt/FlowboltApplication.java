package com.shxv.flowbolt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlowboltApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlowboltApplication.class, args);
	}

}
