# ChapelChat: AI Assistant for Churches

ChapelChat is a powerful, AI-driven chatbot designed to help churches and ministries engage with their communities. It provides instant, accurate answers to questions about church events, beliefs, and schedules, freeing up staff to focus on what matters most.

## Key Features

- **AI-Powered Chatbot**: Leverages the OpenAI API to provide intelligent, context-aware responses.
- **Church-Specific Knowledge**: Easily customizable with church-specific information, including service times, event details, and core beliefs.
- **Secure and Scalable**: Built with Spring Boot and secured with API keys, ensuring reliable and safe operation.
- **Extensible Architecture**: Designed for easy expansion, allowing new features and integrations to be added with minimal effort.

## Getting Started

### Prerequisites

- Java 21
- Maven
- PostgreSQL

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/ChapelChat.git
   ```
2. **Configure the application**:
   - Create a `application-local.yaml` file in `src/main/resources`.
   - Add your PostgreSQL and OpenAI API key details:
     ```yaml
     spring:
       datasource:
         url: jdbc:postgresql://localhost:5432/your-db
         username: your-username
         password: your-password
     openai:
       api:
         key: your-openai-api-key
     ```
3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

## Project Structure

ChapelChat follows a standard Spring Boot project structure:

- **`src/main/java`**: Contains the core application code.
  - **`config`**: Houses configuration classes for security, API keys, and web settings.
  - **`controller`**: Manages incoming web requests and routes them to the appropriate services.
  - **`service`**: Implements the business logic, including AI integration and data management.
  - **`entity`**: Defines the database schema and entities.
  - **`repository`**: Provides an interface for database operations.
- **`src/main/resources`**: Includes configuration files, database migrations, and static assets.
  - **`churches`**: Stores church-specific profiles in JSON format.
  - **`db/changelog`**: Contains Liquibase scripts for database schema management.

## Usage

To ask a question, send a POST request to the `/ask` endpoint with your API key and query:

```bash
curl -X POST http://localhost:8080/ask \
     -H "X-API-KEY: your-api-key" \
     -H "Content-Type: application/json" \
     -d '{"question": "What time is the Sunday service?"}'
```

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes.

## Scheduled Jobs

- **ChatLogCleanupJob**: This job runs daily at 2:00 AM and deletes chat logs older than 90 days (configurable via the `chatlog.retention.days` property). This helps to keep the database clean and manage storage costs.