package rsis.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvConfig {

    static {
        try {
            Dotenv dotenv = Dotenv.load();

            // Load database credentials from .env file
            String dbUrl = dotenv.get("DB_URL");
            String dbUsername = dotenv.get("DB_USERNAME");
            String dbPassword = dotenv.get("DB_PASSWORD");

            if (dbUrl != null) {
                System.setProperty("DB_URL", dbUrl);
            }
            if (dbUsername != null) {
                System.setProperty("DB_USERNAME", dbUsername);
            }
            if (dbPassword != null) {
                System.setProperty("DB_PASSWORD", dbPassword);
            }
        } catch (Exception e) {
            // If .env file is not found, it's okay - environment variables can still be set
            // manually
            System.out.println("Note: .env file not found. Make sure to set environment variables manually.");
        }
    }
}
