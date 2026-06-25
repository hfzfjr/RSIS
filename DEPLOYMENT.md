# Deployment Guide - RSIS

## Environment Variables Configuration

Database credentials are loaded from a `.env` file using dotenv-java library for both development and production.

### Required Environment Variables

- `DB_URL` - JDBC connection URL for PostgreSQL database
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password

### Setup (Development & Production)

1. Copy the example file:
```bash
cp .env.example .env
```

2. Edit `.env` with your actual credentials:
```bash
DB_URL=jdbc:postgresql://your-host:port/database?sslmode=require
DB_USERNAME=your-username
DB_PASSWORD=your-password
```

3. The application will automatically load these values from `.env` file on startup.

### Security Notes

- **NEVER** commit actual credentials to version control
- `.env` file is already in `.gitignore`
- `.env.example` is provided as a template with placeholders
- For production-grade security, consider using secrets management tools (AWS Secrets Manager, HashiCorp Vault) instead of .env files
- Ensure `.env` file has proper file permissions (chmod 600 on Linux/Mac)

### Building and Running

```bash
# Build the application
mvn clean package

# Run the application
java -jar target/rsis-0.0.1-SNAPSHOT.jar
```

The application will automatically load environment variables from the `.env` file in the project root. If the `.env` file is not found, the application will fail to start with a configuration error.
