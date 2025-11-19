package org.pranid.kafka.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pranid.kafka.model.Person;
import org.pranid.kafka.service.PersonService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonController Unit Tests")
class PersonControllerTest {

    @Mock
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Mock
    private PersonService personService;

    @InjectMocks
    private PersonController personController;

    private WebTestClient webTestClient;
    private Person testPerson;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(personController).build();
        testPerson = new Person("John", "male", 25);
    }

    @Test
    @DisplayName("Should get all persons successfully")
    void testGetAllPersons() {
        // Given
        Person person2 = new Person("Jane", "female", 30);
        when(personService.getAllPersons()).thenReturn(Flux.just(testPerson, person2));

        // When & Then
        webTestClient.get()
                .uri("/person/all")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Person.class)
                .hasSize(2)
                .contains(testPerson, person2);

        verify(personService, times(1)).getAllPersons();
    }

    @Test
    @DisplayName("Should create person via Kafka successfully")
    void testCreatePerson() {
        // Given
        SendResult<Object, Object> sendResult = mock(SendResult.class);
        CompletableFuture<SendResult<Object, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

        // When & Then
        webTestClient.get()
                .uri("/person/create-person/John/male/25")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").exists();

        verify(kafkaTemplate, times(1)).send(eq("person"), any(Person.class));
    }

    @Test
    @DisplayName("Should handle Kafka error when creating person")
    void testCreatePersonError() {
        // Given
        CompletableFuture<SendResult<Object, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

        // When & Then
        webTestClient.get()
                .uri("/person/create-person/John/male/25")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("failure")
                .jsonPath("$.message").value(msg -> msg.toString().contains("Failed to send message"));

        verify(kafkaTemplate, times(1)).send(eq("person"), any(Person.class));
    }
}
