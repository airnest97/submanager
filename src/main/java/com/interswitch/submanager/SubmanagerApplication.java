package com.interswitch.submanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@EntityScan(basePackages = {"com.interswitch.**"})
@EnableJpaRepositories(basePackages = {"com.interswitch.**"})
@SpringBootApplication(scanBasePackages = {"com.interswitch.**"})
public class SubmanagerApplication {
	public static void main(String[] args) {
		SpringApplication.run(SubmanagerApplication.class, args);
	}
}
