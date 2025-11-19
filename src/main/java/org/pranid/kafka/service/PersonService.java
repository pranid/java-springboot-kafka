package org.pranid.kafka.service;

import lombok.RequiredArgsConstructor;
import org.pranid.kafka.model.Person;
import org.pranid.kafka.repository.PersonRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;

    public Mono<Person> createPerson(Person person) {
        return personRepository.insertPerson(person);
    }

    public Mono<Person> getPersonById(String id) {
        return personRepository.findById(id);
    }

    public Flux<Person> getAllPersons() {
        return personRepository.findAll();
    }

    public Mono<Person> updatePerson(Person person) {
        return personRepository.updatePerson(person);
    }

    public Mono<Void> deletePersonById(String id) {
        return personRepository.deleteById(id);
    }
}
