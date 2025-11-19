package org.pranid.kafka.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pranid.kafka.model.Person;
import org.pranid.kafka.repository.PersonRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonService Unit Tests")
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    private Person testPerson;

    @BeforeEach
    void setUp() {
        testPerson = new Person("John Doe", "male", 30);
    }

    @Test
    @DisplayName("Should create person successfully")
    void testCreatePerson() {
        // Given
        when(personRepository.insertPerson(any(Person.class)))
                .thenReturn(Mono.just(testPerson));

        // When
        Mono<Person> result = personService.createPerson(testPerson);

        // Then
        StepVerifier.create(result)
                .expectNext(testPerson)
                .verifyComplete();

        verify(personRepository, times(1)).insertPerson(testPerson);
    }

    @Test
    @DisplayName("Should get person by ID successfully")
    void testGetPersonById() {
        // Given
        String personId = "123";
        when(personRepository.findById(anyString()))
                .thenReturn(Mono.just(testPerson));

        // When
        Mono<Person> result = personService.getPersonById(personId);

        // Then
        StepVerifier.create(result)
                .expectNext(testPerson)
                .verifyComplete();

        verify(personRepository, times(1)).findById(personId);
    }

    @Test
    @DisplayName("Should get all persons successfully")
    void testGetAllPersons() {
        // Given
        Person person2 = new Person("Jane Doe", "female", 25);
        when(personRepository.findAll())
                .thenReturn(Flux.just(testPerson, person2));

        // When
        Flux<Person> result = personService.getAllPersons();

        // Then
        StepVerifier.create(result)
                .expectNext(testPerson)
                .expectNext(person2)
                .verifyComplete();

        verify(personRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update person successfully")
    void testUpdatePerson() {
        // Given
        when(personRepository.updatePerson(any(Person.class)))
                .thenReturn(Mono.just(testPerson));

        // When
        Mono<Person> result = personService.updatePerson(testPerson);

        // Then
        StepVerifier.create(result)
                .expectNext(testPerson)
                .verifyComplete();

        verify(personRepository, times(1)).updatePerson(testPerson);
    }

    @Test
    @DisplayName("Should delete person by ID successfully")
    void testDeletePersonById() {
        // Given
        String personId = "123";
        when(personRepository.deleteById(anyString()))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = personService.deletePersonById(personId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(personRepository, times(1)).deleteById(personId);
    }

    @Test
    @DisplayName("Should handle error when person not found")
    void testGetPersonByIdNotFound() {
        // Given
        String personId = "999";
        when(personRepository.findById(anyString()))
                .thenReturn(Mono.empty());

        // When
        Mono<Person> result = personService.getPersonById(personId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(personRepository, times(1)).findById(personId);
    }
}
