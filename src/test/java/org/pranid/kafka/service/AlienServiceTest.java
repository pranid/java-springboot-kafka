package org.pranid.kafka.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pranid.kafka.model.Alien;
import org.pranid.kafka.repository.AlienRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlienService Unit Tests")
class AlienServiceTest {

    @Mock
    private AlienRepository alienRepository;

    @InjectMocks
    private AlienService alienService;

    private Alien testAlien;

    @BeforeEach
    void setUp() {
        testAlien = new Alien("Zorg", "green");
    }

    @Test
    @DisplayName("Should create alien successfully")
    void testCreateAlien() {
        // Given
        when(alienRepository.insertAlien(any(Alien.class)))
                .thenReturn(Mono.just(testAlien));

        // When
        Mono<Alien> result = alienService.createAlien(testAlien);

        // Then
        StepVerifier.create(result)
                .expectNext(testAlien)
                .verifyComplete();

        verify(alienRepository, times(1)).insertAlien(testAlien);
    }

    @Test
    @DisplayName("Should get all aliens successfully")
    void testGetAllAliens() {
        // Given
        Alien alien2 = new Alien("Klaatu", "purple");
        when(alienRepository.findAll())
                .thenReturn(Flux.just(testAlien, alien2));

        // When
        Flux<Alien> result = alienService.getAllAliens();

        // Then
        StepVerifier.create(result)
                .expectNext(testAlien)
                .expectNext(alien2)
                .verifyComplete();

        verify(alienRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get alien by ID successfully")
    void testGetAlienById() {
        // Given
        String alienId = "123";
        when(alienRepository.findById(anyString()))
                .thenReturn(Mono.just(testAlien));

        // When
        Mono<Alien> result = alienService.getAlienById(alienId);

        // Then
        StepVerifier.create(result)
                .expectNext(testAlien)
                .verifyComplete();

        verify(alienRepository, times(1)).findById(alienId);
    }

    @Test
    @DisplayName("Should delete alien by ID successfully")
    void testDeleteAlienById() {
        // Given
        String alienId = "123";
        when(alienRepository.deleteById(anyString()))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = alienService.deleteAlienById(alienId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(alienRepository, times(1)).deleteById(alienId);
    }
}
