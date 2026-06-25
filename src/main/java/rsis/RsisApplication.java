package rsis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import rsis.config.EnvConfig;

@SpringBootApplication
public class RsisApplication {

	public static void main(String[] args) {
		// Load environment variables from .env file before Spring Boot starts
		new EnvConfig();
		SpringApplication.run(RsisApplication.class, args);
	}

}
