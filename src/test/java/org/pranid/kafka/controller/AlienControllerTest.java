package org.pranid.kafka.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pranid.kafka.model.Alien;
import org.pranid.kafka.producer.AlienKafkaProducer;
import org.pranid.kafka.service.AlienService;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlienController Unit Tests")
class AlienControllerTest {

    @Mock
    private AlienKafkaProducer alienKafkaProducer;

    @Mock
    private AlienService alienService;

    @InjectMocks
    private AlienController alienController;

    private WebTestClient webTestClient;
    private Alien testAlien;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(alienController).build();
        testAlien = new Alien("Zorg", "green");
    }

    @Test
    @DisplayName("Should get all aliens successfully")
    void testGetAllAliens() {
        // Given
        Alien alien2 = new Alien("Klaatu", "purple");
        when(alienService.getAllAliens()).thenReturn(Flux.just(testAlien, alien2));

        // When & Then
        webTestClient.get()
                .uri("/alien/all")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Alien.class)
                .hasSize(2)
                .contains(testAlien, alien2);

        verify(alienService, times(1)).getAllAliens();
    }

    @Test
    @DisplayName("Should send alien message with valid green color")
    void testSendAlienMessageGreen() {
        // Given
        doNothing().when(alienKafkaProducer).sendAlienMessage(any(Alien.class));

        // When & Then
        webTestClient.get()
                .uri("/alien/send/Zorg/green")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").exists();

        verify(alienKafkaProducer, times(1)).sendAlienMessage(any(Alien.class));
    }

    @Test
    @DisplayName("Should send alien message with valid purple color")
    void testSendAlienMessagePurple() {
        // Given
        doNothing().when(alienKafkaProducer).sendAlienMessage(any(Alien.class));

        // When & Then
        webTestClient.get()
                .uri("/alien/send/Klaatu/purple")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success");

        verify(alienKafkaProducer, times(1)).sendAlienMessage(any(Alien.class));
    }

    @Test
    @DisplayName("Should reject invalid color")
    void testSendAlienMessageInvalidColor() {
        // When & Then
        webTestClient.get()
                .uri("/alien/send/Zyx/blue")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error")
                .jsonPath("$.message").isEqualTo("Invalid color. Must be 'green' or 'purple'");

        verify(alienKafkaProducer, never()).sendAlienMessage(any(Alien.class));
    }

    @Test
    @DisplayName("Should handle error when sending message")
    void testSendAlienMessageError() {
        // Given
        doThrow(new RuntimeException("Kafka error"))
                .when(alienKafkaProducer).sendAlienMessage(any(Alien.class));

        // When & Then
        webTestClient.get()
                .uri("/alien/send/Zorg/green")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("failure")
                .jsonPath("$.message").value(msg -> msg.toString().contains("Kafka error"));

        verify(alienKafkaProducer, times(1)).sendAlienMessage(any(Alien.class));
    }
}
