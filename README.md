# ChapelChat: AI Assistant for Small Businesses and Churches

ChapelChat is a powerful, AI-driven chatbot designed to help small businesses and churches engage with their communities. It provides instant, accurate answers to questions about business hours, product information, church events, beliefs, and schedules, freeing up staff to focus on what matters most.

## Key Features

- **AI-Powered Chatbot**: Leverages the OpenAI API to provide intelligent, context-aware responses.
- **Customizable Knowledge Base**: Easily customizable with business- or church-specific information, including service times, event details, product catalogs, and core beliefs.
- **Secure and Scalable**: Built with Spring Boot and secured with API keys, ensuring reliable and safe operation.
- **Extensible Architecture**: Designed for easy expansion, allowing new features and integrations to be added with minimal effort.
- **Data Validation**: Includes a validation script to ensure that organization profiles adhere to a defined schema.
- **Chat Analytics and Sentiment Analysis**: Retrieve actionable insight on what your customers or parishioners are requesting the most.
## Getting Started

### Prerequisites

- Java 21
- Maven
- Docker (Recommend using Docker Desktop: https://docs.docker.com/desktop/)

### Installation and Configuration

1. **Clone the repository**:

   ```bash
   git clone https://github.com/your-username/ChapelChat.git
   ```

2. **Start the database**:

   Use Docker Compose to start the PostgreSQL database:

   ```bash
   docker-compose up -d
   ```

3. **Configure the application**:

   - Local configurations are in the `application-local.yaml` file in `src/main/resources`.
   - Add your PostgreSQL and OpenAI API key details:
   
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

4. **Run the application**:

   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```
5. **(Optional) Test the Application**:
   ```bash
   mvn test
   ```

## Database

The application uses a PostgreSQL database. Database schema changes are managed with Liquibase.

- If you need to clear Liquibase checksums, you can run the following command:

```bash
liquibase clearCheckSums --url=jdbc:postgresql://localhost:5432/chapelchat --username=chapel --password=chapel
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
  - **`churches`**: Stores organization-specific profiles in JSON format.
  - **`db/changelog`**: Contains Liquibase scripts for database schema management.

## Data Validation

The project includes a Node.js script to validate the organization profile JSON files against a schema. To run the validation:

1. **Navigate to the `churches` directory**:

   ```bash
   cd src/main/resources/churches
   ```

2. **Install dependencies**:

   ```bash
   npm install ajv
   ```

3. **Run the validation script**:

   ```bash
   node validate.js
   ```

## Usage

To ask a question, send a POST request to the `/ask` endpoint with your API key and query:

### Note

This is MVP. Before production relese, API Keys will not be bundled directly into the frontend. 

```bash
curl -X POST http://localhost:8080/ask \
     -H "X-API-KEY: your-api-key" \
     -H "Content-Type: application/json" \
     -d '{"question": "What time is the Sunday service?"}'
```

## Scheduled Jobs

- **ChatLogCleanupJob**: This job runs daily at 2:00 AM and deletes chat logs older than 90 days (configurable via the `chatlog.retention.days` property). This helps to keep the database clean and manage storage costs.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes.

