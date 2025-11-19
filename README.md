# Java Spring Boot Kafka Integration

A production-ready Spring Boot application demonstrating event-driven architecture using Apache Kafka for message streaming and MongoDB for reactive data persistence.

## Technology Stack

- **Java 25** - Latest LTS Java version
- **Spring Boot 3.5.6** - Core framework
- **Spring WebFlux** - Reactive web framework
- **Spring Kafka** - Kafka integration
- **Spring Data MongoDB Reactive** - Reactive MongoDB integration
- **Apache Kafka** - Distributed event streaming platform
- **MongoDB 7.0.14** - NoSQL database
- **Lombok** - Code generation
- **Log4j2** - Structured logging with JSON layout
- **SpringDoc OpenAPI** - API documentation
- **Micrometer + Prometheus** - Metrics and monitoring
- **Testcontainers** - Integration testing
- **Gradle** - Build tool

## Architecture & Design Patterns

### Reactive Architecture
This application follows a fully reactive programming model using Project Reactor:
- **Non-blocking I/O**: All database and web operations are asynchronous
- **Backpressure handling**: Proper flow control between producers and consumers
- **Reactive streams**: Uses `Mono` and `Flux` for reactive data handling

### Event-Driven Architecture
- **Kafka Topics**: Messages are published to Kafka topics and consumed asynchronously
- **Multi-topic Consumer**: Single consumer handles multiple message types using `@KafkaHandler`
- **Event Sourcing**: MongoDB listeners track entity lifecycle events
- **Decoupled Components**: Producers and consumers are independent

### Layered Architecture
```
Controller → Service → Repository → Database
     ↓
Producer → Kafka → Consumer → Service → Repository
```

## Project Structure

```
src/main/java/org/pranid/kafka/
├── config/              # Configuration classes
│   └── KafkaConfig.java # Kafka producer/consumer configuration
├── controller/          # REST API endpoints
│   ├── PersonController.java
│   └── AlienController.java
├── service/             # Business logic layer
│   ├── PersonService.java
│   └── AlienService.java
├── repository/          # Data access layer
│   ├── PersonRepository.java
│   └── AlienRepository.java
├── producer/            # Kafka message producers
│   ├── PersonKafkaProducer.java
│   └── AlienKafkaProducer.java
├── consumer/            # Kafka message consumers
│   ├── MultiTopicKafkaConsumer.java
│   └── AlienKafkaConsumer.java
├── listener/            # MongoDB event listeners
│   └── PersonMongoListener.java
├── model/               # Domain entities
│   ├── Person.java
│   ├── Alien.java
│   └── Pet.java
└── Application.java     # Main application class

src/main/resources/
├── application.yml      # Application configuration
└── log4j2.yml          # Logging configuration

src/test/java/org/pranid/kafka/
├── controller/          # Controller unit tests
├── service/            # Service unit tests
└── integration/        # Integration tests with Testcontainers
```

## Implementation Guidelines

### 1. Model Layer (Domain Entities)

Models represent data structures for both MongoDB documents and Kafka messages:

```java
@Data                                    // Lombok: generates getters, setters, toString, etc.
@NoArgsConstructor                       // Required for deserialization
@AllArgsConstructor                      // Convenient for object creation
@Document(collection = "persons")       // MongoDB collection mapping
public class Person implements Serializable {  // Serializable for Kafka
    private String name;
    private String gender;
    private int age;
}
```

**Guidelines:**
- Use `@Document` for MongoDB entities
- Implement `Serializable` for Kafka message types
- Use Lombok annotations to reduce boilerplate
- Keep models simple (POJOs)

### 2. Repository Layer (Data Access)

Repositories handle reactive database operations:

```java
@Repository
public interface PersonRepository extends ReactiveMongoRepository<Person, String> {
    Mono<Person> insertPerson(Person person);
    Mono<Person> updatePerson(Person person);
}
```

**Guidelines:**
- Extend `ReactiveMongoRepository<Entity, ID>`
- Return `Mono<T>` for single results
- Return `Flux<T>` for multiple results
- Use custom query methods when needed

### 3. Service Layer (Business Logic)

Services orchestrate business operations:

```java
@Service
@RequiredArgsConstructor  // Constructor injection via Lombok
public class PersonService {
    private final PersonRepository personRepository;

    public Mono<Person> createPerson(Person person) {
        return personRepository.insertPerson(person);
    }

    public Flux<Person> getAllPersons() {
        return personRepository.findAll();
    }
}
```

**Guidelines:**
- Use `@RequiredArgsConstructor` for dependency injection
- Keep business logic in services, not controllers
- Return reactive types (`Mono`/`Flux`)
- Handle errors using reactive operators (`onErrorResume`, `doOnError`)

### 4. Controller Layer (REST API)

Controllers expose reactive REST endpoints:

```java
@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {
    private final PersonService personService;
    private final PersonKafkaProducer producer;

    @PostMapping
    public Mono<Person> createPerson(@RequestBody Person person) {
        producer.sendMessage(person);  // Async Kafka publish
        return personService.createPerson(person);
    }

    @GetMapping
    public Flux<Person> getAllPersons() {
        return personService.getAllPersons();
    }
}
```

**Guidelines:**
- Use `@RestController` for REST APIs
- Return `Mono`/`Flux` directly (Spring WebFlux handles subscription)
- Keep controllers thin - delegate to services
- Publish to Kafka for event-driven workflows

### 5. Kafka Producer Configuration

Producers send messages to Kafka topics:

```java
@Component
@RequiredArgsConstructor
public class PersonKafkaProducer {
    private final KafkaTemplate<String, Person> kafkaTemplate;

    public void sendMessage(Person person) {
        kafkaTemplate.send("person", person);
    }
}
```

**Configuration:**
```yaml
spring:
  kafka:
    bootstrap-servers: "localhost:9092"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

**Guidelines:**
- Use `KafkaTemplate<K, V>` for sending messages
- Configure JSON serialization for automatic object conversion
- Topic names should be consistent across producers/consumers
- Consider error handling and retries for production

### 6. Kafka Consumer Configuration

Consumers listen to Kafka topics and process messages:

**Single-topic Consumer:**
```java
@Component
public class AlienKafkaConsumer {
    @KafkaListener(topics = "alien", containerFactory = "alienKafkaListenerContainerFactory")
    public void consume(Alien alien) {
        // Process message
    }
}
```

**Multi-topic Consumer:**
```java
@Component
@KafkaListener(
    topics = {"person", "pet", "alien"},
    containerFactory = "multiTypeKafkaListenerContainerFactory"
)
public class MultiTopicKafkaConsumer {

    @KafkaHandler
    public void person(Person person) { /* handle Person */ }

    @KafkaHandler
    public void pet(Pet pet) { /* handle Pet */ }

    @KafkaHandler
    public void alien(Alien alien) { /* handle Alien */ }

    @KafkaHandler(isDefault = true)
    public void unknown(Object object) { /* handle unknown types */ }
}
```

**Configuration:**
```yaml
spring:
  kafka:
    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      enable-auto-commit: true
```

**Guidelines:**
- Use `@KafkaListener` for topic subscription
- Use `@KafkaHandler` for multi-type message handling in a single consumer
- Configure appropriate container factories in `KafkaConfig`
- Handle errors gracefully within consumer methods
- Use `.subscribe()` when calling reactive services from consumers

### 7. MongoDB Event Listeners

Listeners track entity lifecycle events:

```java
@Component
@RequiredArgsConstructor
public class PersonMongoListener extends AbstractMongoEventListener<Person> {

    @Override
    public void onAfterSave(AfterSaveEvent<Person> event) {
        // React to save events
    }

    @Override
    public void onAfterDelete(AfterDeleteEvent<Person> event) {
        // React to delete events
    }
}
```

**Guidelines:**
- Extend `AbstractMongoEventListener<T>`
- Override lifecycle methods as needed
- Useful for auditing, notifications, or triggering side effects

### 8. Testing Strategy

The project includes comprehensive testing:

**Unit Tests:**
- Test individual components in isolation
- Mock dependencies using Mockito
- Focus on business logic

**Integration Tests:**
- Use Testcontainers for real Kafka and MongoDB instances
- Test full request-response flows
- Verify message consumption and persistence

```java
@SpringBootTest
@Testcontainers
public class KafkaIntegrationTest extends AbstractIntegrationTest {
    @Container
    static KafkaContainer kafka = new KafkaContainer(/* config */);

    @Container
    static MongoDBContainer mongodb = new MongoDBContainer(/* config */);
}
```

**Guidelines:**
- Extend `AbstractIntegrationTest` for shared test configuration
- Use `@Testcontainers` for integration tests
- Use `Awaitility` for async assertion verification
- Test both happy paths and error scenarios

## Docker Setup

The project uses Docker Compose to orchestrate Kafka, Kafka UI, and MongoDB.

### Services

1. **Apache Kafka (KRaft mode)**
   - Port: `9092` (external), `19092` (internal)
   - Controller port: `9093`
   - Single-node cluster with 3 default partitions

2. **Kafka UI**
   - Port: `8081`
   - Web interface: http://localhost:8081
   - Monitor topics, messages, consumer groups

3. **MongoDB**
   - Port: `27017`
   - Credentials: `developer` / `developer`
   - Database: `java-springboot-kafka`

### Docker Commands

#### Start all services:
```bash
docker-compose up -d
```

#### View logs:
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f broker
docker-compose logs -f mongodb
docker-compose logs -f kafka-ui
```

#### Check service status:
```bash
docker-compose ps
```

#### Stop all services:
```bash
docker-compose down
```

#### Stop and remove volumes (data cleanup):
```bash
docker-compose down -v
```

#### Restart a specific service:
```bash
docker-compose restart broker
docker-compose restart mongodb
```

#### Execute commands inside containers:
```bash
# Access Kafka container
docker exec -it broker bash

# Access MongoDB shell
docker exec -it mongodb mongosh -u developer -p developer

# List Kafka topics
docker exec -it broker kafka-topics.sh --bootstrap-server localhost:9092 --list

# Describe a topic
docker exec -it broker kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic person
```

### Data Persistence

Docker volumes are used for data persistence:
- `./kafka_data` - Kafka data
- `./mongo_data` - MongoDB data

These directories are created automatically and persist data across container restarts.

## Running the Application

### Prerequisites
- Java 25
- Docker & Docker Compose
- Gradle (wrapper included)

### Steps

1. **Start infrastructure services:**
   ```bash
   docker-compose up -d
   ```

2. **Verify services are healthy:**
   ```bash
   docker-compose ps
   # Wait for all services to show "healthy" or "running"
   ```

3. **Build the application:**
   ```bash
   ./gradlew clean build
   ```

4. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

   Or run the JAR directly:
   ```bash
   java -jar build/libs/java-springboot-kafka-1.0-SNAPSHOT.jar
   ```

5. **Verify application is running:**
   - Application: http://localhost:8080
   - API Docs: http://localhost:8080/swagger-ui.html
   - Actuator: http://localhost:8080/actuator
   - Metrics: http://localhost:8080/actuator/prometheus

## API Endpoints

### Person API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/persons` | Create a new person (publishes to Kafka) |
| GET | `/api/persons` | Get all persons |
| GET | `/api/persons/{id}` | Get person by ID |
| PUT | `/api/persons/{id}` | Update person |
| DELETE | `/api/persons/{id}` | Delete person |

### Alien API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/aliens` | Create a new alien (publishes to Kafka) |
| GET | `/api/aliens` | Get all aliens |
| GET | `/api/aliens/{id}` | Get alien by ID |
| PUT | `/api/aliens/{id}` | Update alien |
| DELETE | `/api/aliens/{id}` | Delete alien |

### Example Requests

**Create Person:**
```bash
curl -X POST http://localhost:8080/api/persons \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "gender": "Male", "age": 30}'
```

**Get All Persons:**
```bash
curl http://localhost:8080/api/persons
```

## Configuration

Key configuration in `application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://developer:developer@localhost:27017/java-springboot-kafka
  kafka:
    bootstrap-servers: "localhost:9092"
    consumer:
      auto-offset-reset: earliest  # Start from beginning for new consumers
      enable-auto-commit: true
```

## Logging

The application uses Log4j2 with JSON structured logging:
- Configuration: `src/main/resources/log4j2.yml`
- Format: JSON for easy parsing and analysis
- Levels: INFO (default), configurable per package

## Monitoring & Observability

- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/prometheus`
- **Info**: `/actuator/info`
- **Kafka UI**: http://localhost:8081 - Monitor Kafka topics and messages
- **MongoDB**: Connect via MongoDB Compass to `mongodb://developer:developer@localhost:27017`

## Development Workflow

1. **Make code changes**
2. **Run tests:**
   ```bash
   ./gradlew test
   ```
3. **Build application:**
   ```bash
   ./gradlew build
   ```
4. **Run locally:**
   ```bash
   ./gradlew bootRun
   ```
5. **Access Kafka UI** to monitor message flow
6. **Check logs** for debugging

## Testing

### Run all tests:
```bash
./gradlew test
```

### Run specific test class:
```bash
./gradlew test --tests PersonServiceTest
```

### Run with coverage:
```bash
./gradlew test jacocoTestReport
```

## Troubleshooting

### Kafka connection issues
- Verify Kafka is running: `docker-compose ps`
- Check Kafka logs: `docker-compose logs broker`
- Ensure port 9092 is not in use

### MongoDB connection issues
- Verify MongoDB is running: `docker-compose ps`
- Test connection: `docker exec -it mongodb mongosh -u developer -p developer`
- Check credentials in `application.yml`

### Application won't start
- Check Java version: `java -version` (must be Java 25)
- Ensure Docker services are healthy
- Check for port conflicts (8080, 9092, 27017, 8081)

## License

This project is for educational and demonstration purposes.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Write/update tests
5. Submit a pull request
