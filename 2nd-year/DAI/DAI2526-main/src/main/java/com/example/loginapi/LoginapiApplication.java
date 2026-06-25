
package com.example.loginapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.example.loginapi.repository")

public class LoginapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoginapiApplication.class, args);
	}

}