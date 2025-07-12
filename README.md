# Autotroph Service

A lightweight Kotlin-based microservice for interacting with Neo4j graph database, providing RESTful endpoints for managing entities and their relationships.

## Features

- **Entity Management**: Create, read, update, and delete entities in Neo4j
- **Relationship Management**: Create and manage relationships between entities
- **Search Capabilities**: Search entities by name, type, and custom properties
- **RESTful API**: Clean REST endpoints with proper HTTP status codes
- **Lightweight Framework**: Built with Ktor for minimal overhead and fast startup
- **Comprehensive Testing**: Unit and integration tests included
- **Coroutines Support**: Fully asynchronous with Kotlin coroutines

## Technology Stack

- **Kotlin 1.9.22** - Primary programming language
- **Ktor 2.3.7** - Lightweight web framework
- **Neo4j Java Driver** - Direct Neo4j database access
- **Kotlinx Serialization** - JSON serialization
- **Kodein DI** - Lightweight dependency injection
- **Gradle** - Build tool
- **Kotlin Test** - Testing framework
- **MockK** - Mocking framework for Kotlin

## Prerequisites

- Java 21 or higher
- Neo4j database (can be run via Docker)

## Quick Start

### 1. Start Neo4j Database

Using Docker Compose:

```bash
docker-compose up -d
```

Or manually with Docker:

```bash
docker run \
    --name neo4j \
    -p7474:7474 -p7687:7687 \
    -d \
    -v $HOME/neo4j/data:/data \
    -v $HOME/neo4j/logs:/logs \
    -v $HOME/neo4j/import:/var/lib/neo4j/import \
    -v $HOME/neo4j/plugins:/plugins \
    --env NEO4J_AUTH=neo4j/password \
    neo4j:latest
```

### 2. Build and Run the Application

```bash
# Build the application
./gradlew build

# Run the application
./gradlew run
# or use the convenience script
./run.sh
```

The service will start on `http://localhost:8080`

### 3. Access Neo4j Browser

Open `http://localhost:7474` in your browser to access the Neo4j browser interface.
- Username: `neo4j`
- Password: `password`

## API Endpoints

### Entity Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/entities` | Get all entities |
| GET | `/api/v1/entities/{id}` | Get entity by ID |
| GET | `/api/v1/entities/search?name={name}` | Search entities by name |
| GET | `/api/v1/entities/search?type={type}` | Search entities by type |
| GET | `/api/v1/entities/search?nameFragment={fragment}` | Search entities by name fragment |
| POST | `/api/v1/entities` | Create new entity |
| PUT | `/api/v1/entities/{id}` | Update entity |
| DELETE | `/api/v1/entities/{id}` | Delete entity |

### Relationship Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/entities/{id}/related` | Get related entities |
| POST | `/api/v1/entities/{fromId}/relationships/{toId}` | Create relationship |
| DELETE | `/api/v1/entities/{fromId}/relationships/{toId}` | Delete relationship |

### Example Requests

#### Create Entity
```bash
curl -X POST http://localhost:8080/api/v1/entities \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "type": "Person",
    "description": "Software Developer",
    "properties": {
      "age": 30,
      "city": "New York"
    }
  }'
```

#### Create Relationship
```bash
curl -X POST http://localhost:8080/api/v1/entities/1/relationships/2
```

## Configuration

The application can be configured via environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `NEO4J_URI` | `bolt://localhost:7687` | Neo4j connection URI |
| `NEO4J_USERNAME` | `neo4j` | Neo4j username |
| `NEO4J_PASSWORD` | `password` | Neo4j password |
| `NEO4J_DATABASE` | `neo4j` | Neo4j database name |
| `SERVER_PORT` | `8080` | Application server port |

### Neo4j Aura Configuration

To connect to Neo4j Aura (cloud), set the following environment variables:

```bash
export NEO4J_URI=neo4j+s://your-instance-id.databases.neo4j.io
export NEO4J_USERNAME=neo4j
export NEO4J_PASSWORD=your-aura-password
export NEO4J_DATABASE=neo4j
```

You can also copy `.env.example` to `.env` and modify the values as needed.

## Testing

Run all tests:
```bash
./gradlew test
```

Run tests with coverage:
```bash
./gradlew test jacocoTestReport
```

## Health Checks

The application includes basic health endpoints:

- Health: `http://localhost:8080/health`
- Root: `http://localhost:8080/` (service status)

## Development

### Project Structure

```
src/
├── main/kotlin/com/foodchain/autotroph/
│   ├── Application.kt                 # Main Ktor application
│   ├── config/
│   │   └── Neo4jConfig.kt            # Ktor configuration & DI setup
│   ├── controller/
│   │   └── EntityController.kt       # Ktor route definitions
│   ├── model/
│   │   └── Node.kt                   # Serializable data models
│   ├── repository/
│   │   └── EntityRepository.kt       # Neo4j driver-based repositories
│   └── service/
│       └── EntityService.kt          # Async business logic
├── main/resources/
│   └── application.yml               # Ktor configuration
└── test/kotlin/                      # Coroutine-based tests
```

### Adding New Features

1. Define your serializable models in `model/`
2. Create repository classes with Neo4j driver in `repository/`
3. Implement async business logic in `service/`
4. Add Ktor routes in `controller/`
5. Write coroutine-based tests for all layers

## Docker Support

Build Docker image:
```bash
./gradlew buildImage
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.
