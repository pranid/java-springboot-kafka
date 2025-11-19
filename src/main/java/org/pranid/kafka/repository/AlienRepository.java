package org.pranid.kafka.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.pranid.kafka.model.Alien;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Log4j2
public class AlienRepository {
    private static final String COLLECTION = "aliens";
    private final ReactiveMongoTemplate mongoTemplate;

    public Mono<Alien> insertAlien(Alien alien) {
        return mongoTemplate.insert(alien, COLLECTION);
    }

    public Mono<Alien> findById(String id) {
        return mongoTemplate.findById(id, Alien.class, COLLECTION);
    }

    public Flux<Alien> findAll() {
        return mongoTemplate.findAll(Alien.class, COLLECTION);
    }

    public Mono<Alien> updateAlien(Alien alien) {
        return mongoTemplate.save(alien, COLLECTION);
    }

    public Mono<Void> deleteById(String id) {
        return mongoTemplate.remove(
                mongoTemplate.findById(id, Alien.class, COLLECTION)
        ).then();
    }
}
