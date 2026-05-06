package de.skit.grocy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GrocyApplication {
	public static void main(String[] args) {
		SpringApplication.run(GrocyApplication.class, args);
	}
}
