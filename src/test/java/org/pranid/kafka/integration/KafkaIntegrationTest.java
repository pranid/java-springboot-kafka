package org.pranid.kafka.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.pranid.kafka.AbstractIntegrationTest;
import org.pranid.kafka.model.Alien;
import org.pranid.kafka.model.Person;
import org.pranid.kafka.producer.AlienKafkaProducer;
import org.pranid.kafka.producer.PersonKafkaProducer;
import org.pranid.kafka.service.AlienService;
import org.pranid.kafka.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.context.EmbeddedKafka;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Integration tests for Kafka producer-consumer flow.
 * Tests the entire flow from producing messages to consuming and persisting to MongoDB.
 */
@DisplayName("Kafka Integration Tests")
class KafkaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PersonKafkaProducer personKafkaProducer;

    @Autowired
    private AlienKafkaProducer alienKafkaProducer;

    @Autowired
    private PersonService personService;

    @Autowired
    private AlienService alienService;

    @Test
    @DisplayName("Should produce and consume Person message successfully")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testPersonKafkaFlow() {
        // Given
        Person person = new Person("Integration Test Person", "male", 35);

        // When - Send message to Kafka
        personKafkaProducer.sendPersonCreatedEvent(person);

        // Then - Wait for message to be consumed and saved to MongoDB
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    StepVerifier.create(personService.getAllPersons()
                                    .filter(p -> p.getName().equals("Integration Test Person")))
                            .expectNextCount(1)
                            .verifyComplete();
                });
    }

    @Test
    @DisplayName("Should produce and consume Alien message successfully")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testAlienKafkaFlow() {
        // Given
        Alien alien = new Alien("Test Alien", "green");

        // When - Send message to Kafka
        alienKafkaProducer.sendAlienMessage(alien);

        // Then - Wait for message to be consumed and saved to MongoDB
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    StepVerifier.create(alienService.getAllAliens()
                                    .filter(a -> a.getName().equals("Test Alien")))
                            .expectNextCount(1)
                            .verifyComplete();
                });
    }

    @Test
    @DisplayName("Should handle multiple Person messages")
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testMultiplePersonMessages() {
        // Given
        Person person1 = new Person("Bulk Test 1", "male", 20);
        Person person2 = new Person("Bulk Test 2", "female", 25);
        Person person3 = new Person("Bulk Test 3", "male", 30);

        // When
        personKafkaProducer.sendPersonCreatedEvent(person1);
        personKafkaProducer.sendPersonCreatedEvent(person2);
        personKafkaProducer.sendPersonCreatedEvent(person3);

        // Then
        await().atMost(Duration.ofSeconds(8))
                .untilAsserted(() -> {
                    StepVerifier.create(personService.getAllPersons()
                                    .filter(p -> p.getName().startsWith("Bulk Test")))
                            .expectNextCount(3)
                            .verifyComplete();
                });
    }

    @Test
    @DisplayName("Should handle multiple Alien messages with different colors")
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testMultipleAlienMessages() {
        // Given
        Alien greenAlien = new Alien("Green Alien", "green");
        Alien purpleAlien = new Alien("Purple Alien", "purple");

        // When
        alienKafkaProducer.sendAlienMessage(greenAlien);
        alienKafkaProducer.sendAlienMessage(purpleAlien);

        // Then
        await().atMost(Duration.ofSeconds(8))
                .untilAsserted(() -> {
                    StepVerifier.create(alienService.getAllAliens()
                                    .filter(a -> a.getName().contains("Alien")))
                            .expectNextMatches(a -> a.getColor().equals("green") || a.getColor().equals("purple"))
                            .expectNextMatches(a -> a.getColor().equals("green") || a.getColor().equals("purple"))
                            .verifyComplete();
                });
    }
}
