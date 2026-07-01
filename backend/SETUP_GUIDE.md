# Backend Setup Guide

## Prerequisites
- Java 21+
- Maven 3.8+
- PostgreSQL 14+
- OpenAI API Key (https://platform.openai.com/api-keys)

## Environment Configuration

### 1. Database Setup
```bash
# Create PostgreSQL database
CREATE DATABASE codeinsight;
```

Update `application.yaml` with your database credentials:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/codeinsight
    username: postgres
    password: your-password
```

### 2. OpenAI API Key
Set environment variable before running the application:

**Windows (PowerShell):**
```powershell
$env:OPENAI_API_KEY = "YOUR_OPENAI_API_KEY"
```

**Windows (CMD):**
```cmd
set OPENAI_API_KEY=YOUR_OPENAI_API_KEY
```

**Linux/Mac:**
```bash
export OPENAI_API_KEY="YOUR_OPENAI_API_KEY"
```

**Or add to `application.yaml`:**
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
```

### 3. JWT Secret
Update JWT secret in `application.yaml` (minimum 32 characters):
```yaml
jwt:
  secret: your-super-secret-jwt-key-with-at-least-32-characters
  expiration: 86400000
```

## Running the Application

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

The application will start at `http://localhost:8080`

### Access Swagger UI
http://localhost:8080/swagger-ui.html

## Security Notes
- ⚠️ **NEVER** commit `application.yaml` with real secrets to GitHub
- Use environment variables for sensitive data in production
- The `.gitignore` file excludes `application.yaml` from version control
- Use `application.yaml.example` as a template for configuration

## Deployment

### Setting Environment Variables on Production Servers

**Railway.com:**
- Add environment variables in project settings
- `OPENAI_API_KEY`, `DATABASE_URL`, `JWT_SECRET`

**Render.com:**
- Add environment variables in service settings
- Set secrets in "Environment" section

**Docker:**
```dockerfile
ENV OPENAI_API_KEY=your-api-key
ENV DATABASE_URL=jdbc:postgresql://...
```

```bash
docker run -e OPENAI_API_KEY=<your-openai-api-key> codeinsight-backend
```
