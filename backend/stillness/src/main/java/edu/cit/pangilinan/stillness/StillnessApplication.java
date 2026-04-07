package edu.cit.pangilinan.stillness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {"edu.cit.pangilinan.stillness", "com.stillness"})
@EnableAsync
public class StillnessApplication {

	public static void main(String[] args) {
		SpringApplication.run(StillnessApplication.class, args);
	}

}
