# ChapelChat

ChapelChat is a backend service that provides a conversational AI interface for churches. It allows users to ask questions and get information about a specific church's profile.

## Getting Started

To get started with ChapelChat, you'll need to have Java and Maven installed.

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/emikac123-hub/ChapelChat.git
    ```

2.  **Navigate to the project directory:**

    ```bash
    cd ChapelChat
    ```

3.  **Run the application:**

    ```bash
    ./mvnw spring-boot:run

    run locally - mvn spring-boot:run -Dspring.profiles.active=local
    ```

The application will start on port 8080.

## API

The primary endpoint for ChapelChat is `/ask`.

### POST /ask

This endpoint takes a JSON object with a `question` and a `churchId` and returns a response from the AI.

**Request:**

```json
{
  "question": "What are your service times?",
  "churchId": "cornerstone-catholic"
}
```

**Response:**

```json
{
  "response": "Our service times are Sundays at 9:00 AM and 11:00 AM."
}
```

## Future Development

*   **Frontend Widget:** A widget that churches can embed on their websites to provide a chat interface to the `/ask` API.
*   **Dashboard:** A dashboard that allows churches to customize their profiles and view analytics.

## Project Structure

The project is a standard Spring Boot application with the following structure:

```
.
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── erikmikac
│   │   │           └── ChapelChat
│   │   │               ├── ChapelChatApplication.java
│   │   │               ├── config
│   │   │               ├── controller
│   │   │               ├── exceptions
│   │   │               ├── model
│   │   │               └── service
│   │   └── resources
│   │       ├── application.properties
│   │       ├── churches
│   │       ├── static
│   │       └── templates
│   └── test
└── pom.xml
```

## Contributing

Contributions are welcome! Please feel free to submit a pull request.
