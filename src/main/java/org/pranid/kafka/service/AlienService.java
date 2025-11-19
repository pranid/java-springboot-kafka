package org.pranid.kafka.service;

import lombok.RequiredArgsConstructor;
import org.pranid.kafka.model.Alien;
import org.pranid.kafka.repository.AlienRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AlienService {
    private final AlienRepository alienRepository;

    public Mono<Alien> createAlien(Alien alien) {
        return alienRepository.insertAlien(alien);
    }

    public Mono<Alien> getAlienById(String id) {
        return alienRepository.findById(id);
    }

    public Flux<Alien> getAllAliens() {
        return alienRepository.findAll();
    }

    public Mono<Alien> updateAlien(Alien alien) {
        return alienRepository.updateAlien(alien);
    }

    public Mono<Void> deleteAlienById(String id) {
        return alienRepository.deleteById(id);
    }
}
