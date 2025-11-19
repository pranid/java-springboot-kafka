package org.pranid.kafka.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pranid.kafka.AbstractIntegrationTest;
import org.pranid.kafka.model.Alien;
import org.pranid.kafka.model.Person;
import org.pranid.kafka.repository.AlienRepository;
import org.pranid.kafka.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Integration tests for MongoDB repository operations.
 * Tests CRUD operations with actual MongoDB container.
 */
@DisplayName("MongoDB Integration Tests")
class MongoDBIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AlienRepository alienRepository;

    @BeforeEach
    void cleanup() {
        // Clean up before each test
        personRepository.findAll()
                .flatMap(person -> personRepository.deleteById("test-id"))
                .blockLast();

        alienRepository.findAll()
                .flatMap(alien -> alienRepository.deleteById("test-id"))
                .blockLast();
    }

    @Test
    @DisplayName("Should insert and retrieve Person from MongoDB")
    void testPersonInsertAndRetrieve() {
        // Given
        Person person = new Person("MongoDB Test Person", "male", 40);

        // When
        Mono<Person> insertedPerson = personRepository.insertPerson(person);

        // Then
        StepVerifier.create(insertedPerson)
                .expectNextMatches(p ->
                        p.getName().equals("MongoDB Test Person") &&
                        p.getGender().equals("male") &&
                        p.getAge() == 40
                )
                .verifyComplete();

        // Verify retrieval
        Flux<Person> allPersons = personRepository.findAll();
        StepVerifier.create(allPersons)
                .expectNextMatches(p -> p.getName().equals("MongoDB Test Person"))
                .thenCancel()
                .verify();
    }

    @Test
    @DisplayName("Should insert and retrieve Alien from MongoDB")
    void testAlienInsertAndRetrieve() {
        // Given
        Alien alien = new Alien("MongoDB Test Alien", "purple");

        // When
        Mono<Alien> insertedAlien = alienRepository.insertAlien(alien);

        // Then
        StepVerifier.create(insertedAlien)
                .expectNextMatches(a ->
                        a.getName().equals("MongoDB Test Alien") &&
                        a.getColor().equals("purple")
                )
                .verifyComplete();

        // Verify retrieval
        Flux<Alien> allAliens = alienRepository.findAll();
        StepVerifier.create(allAliens)
                .expectNextMatches(a -> a.getName().equals("MongoDB Test Alien"))
                .thenCancel()
                .verify();
    }

    @Test
    @DisplayName("Should update Person in MongoDB")
    void testPersonUpdate() {
        // Given
        Person person = new Person("Update Test", "male", 25);
        Person savedPerson = personRepository.insertPerson(person).block();

        // When - Update age
        savedPerson.setAge(30);
        Mono<Person> updatedPerson = personRepository.updatePerson(savedPerson);

        // Then
        StepVerifier.create(updatedPerson)
                .expectNextMatches(p -> p.getAge() == 30)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update Alien in MongoDB")
    void testAlienUpdate() {
        // Given
        Alien alien = new Alien("Update Alien", "green");
        Alien savedAlien = alienRepository.insertAlien(alien).block();

        // When - Update color
        savedAlien.setColor("purple");
        Mono<Alien> updatedAlien = alienRepository.updateAlien(savedAlien);

        // Then
        StepVerifier.create(updatedAlien)
                .expectNextMatches(a -> a.getColor().equals("purple"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle multiple Person inserts")
    void testMultiplePersonInserts() {
        // Given
        Person person1 = new Person("Batch 1", "male", 20);
        Person person2 = new Person("Batch 2", "female", 25);
        Person person3 = new Person("Batch 3", "male", 30);

        // When
        Flux<Person> insertedPersons = Flux.concat(
                personRepository.insertPerson(person1),
                personRepository.insertPerson(person2),
                personRepository.insertPerson(person3)
        );

        // Then
        StepVerifier.create(insertedPersons)
                .expectNextCount(3)
                .verifyComplete();

        // Verify all are in database
        Flux<Person> allPersons = personRepository.findAll()
                .filter(p -> p.getName().startsWith("Batch"));

        StepVerifier.create(allPersons)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle multiple Alien inserts")
    void testMultipleAlienInserts() {
        // Given
        Alien alien1 = new Alien("Batch Alien 1", "green");
        Alien alien2 = new Alien("Batch Alien 2", "purple");

        // When
        Flux<Alien> insertedAliens = Flux.concat(
                alienRepository.insertAlien(alien1),
                alienRepository.insertAlien(alien2)
        );

        // Then
        StepVerifier.create(insertedAliens)
                .expectNextCount(2)
                .verifyComplete();

        // Verify all are in database
        Flux<Alien> allAliens = alienRepository.findAll()
                .filter(a -> a.getName().startsWith("Batch Alien"));

        StepVerifier.create(allAliens)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should retrieve empty result when no data exists")
    void testEmptyResults() {
        // When - Query for non-existent data
        Flux<Person> persons = personRepository.findAll()
                .filter(p -> p.getName().equals("NonExistent"));

        // Then
        StepVerifier.create(persons)
                .verifyComplete();
    }
}
