# ChapelChat: AI Assistant for Small Businesses and Churches

ChapelChat is a Spring Boot application that delivers an AI-driven chatbot for organizations. It answers questions about events, products, or beliefs by combining a customizable knowledge base with OpenAI.

## Key Features

- **AI-Powered Chatbot**: Uses OpenAI's API to generate context-aware responses.
- **Customizable Knowledge Base**: Load business or church data such as service times, product lists, or beliefs.
- **Secure and Scalable**: API key authentication and Dockerized services ensure reliable, safe operation.
- **Extensible Architecture**: Modular design makes adding new integrations straightforward.
- **Data Validation**: Node.js script validates organization profile JSON against a schema.

## Getting Started

### Prerequisites

- Java 21
- Maven
- Docker

### Installation and Configuration

1. **Clone the repository**

   ```bash
   git clone https://github.com/your-username/ChapelChat.git
   ```

2. **Start the database**

   ```bash
   docker-compose up -d
   ```

3. **Configure the application**

   Create `src/main/resources/application-local.yaml`:

   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/chapelchat
       username: chapel
       password: chapel
   openai:
     api:
       key: your-openai-api-key
   ```

4. **Run the application**

   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

## Database

ChapelChat uses PostgreSQL with Liquibase for schema migrations. To clear Liquibase checksums:

```bash
liquibase clearCheckSums --url=jdbc:postgresql://localhost:5432/chapelchat --username=chapel --password=chapel
```

## Project Structure

```text
src/
├── main/
│   ├── java/com/erikmikac/ChapelChat
│   │   ├── config/      # Security, API key, and web configs
│   │   ├── controller/  # REST controllers
│   │   ├── service/     # Business logic and OpenAI calls
│   │   ├── entity/      # JPA entities
│   │   └── repository/  # Spring Data repositories
│   └── resources/
│       ├── churches/    # Organization profiles
│       └── db/changelog # Liquibase migrations
└── test/java            # Unit and integration tests
```

## Data Validation

Validate organization profile JSON files:

```bash
cd src/main/resources/churches
npm install ajv
node validate.js
```

## Usage

Send a question to the `/ask` endpoint:

> **Note:** This is an MVP. API keys are currently bundled directly into the frontend.

```bash
curl -X POST http://localhost:8080/ask \
     -H "X-API-KEY: your-api-key" \
     -H "Content-Type: application/json" \
     -d '{"question": "What time is the Sunday service?"}'
```

## Scheduled Jobs

- **ChatLogCleanupJob** – runs daily at 2:00 AM and deletes chat logs older than 90 days (configurable via `chatlog.retention.days`).

## Contributing

Contributions are welcome! Fork the repository and open a pull request.

