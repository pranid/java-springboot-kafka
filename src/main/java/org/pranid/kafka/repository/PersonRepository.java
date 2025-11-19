package org.pranid.kafka.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.pranid.kafka.model.Person;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Log4j2
public class PersonRepository {
    private static final String COLLECTION = "persons";
    private final ReactiveMongoTemplate mongoTemplate;

    public Mono<Person> insertPerson(Person person) {
        return mongoTemplate.insert(person, COLLECTION);
    }

    public Mono<Person> findById(String id) {
        return mongoTemplate.findById(id, Person.class, COLLECTION);
    }

    public Flux<Person> findAll() {
        return mongoTemplate.findAll(Person.class, COLLECTION);
    }

    public Mono<Person> updatePerson(Person person) {
        return mongoTemplate.save(person, COLLECTION);
    }

    public Mono<Void> deleteById(String id) {
        return mongoTemplate.remove(
                mongoTemplate.findById(id, Person.class, COLLECTION)
        ).then();
    }
}
