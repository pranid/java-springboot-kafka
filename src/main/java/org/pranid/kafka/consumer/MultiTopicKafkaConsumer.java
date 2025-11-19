package org.pranid.kafka.consumer;

import org.pranid.kafka.model.Alien;
import org.pranid.kafka.model.Person;
import org.pranid.kafka.model.Pet;
import org.pranid.kafka.service.AlienService;
import org.pranid.kafka.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(
        topics = {"person", "pet", "alien"},
        containerFactory = "multiTypeKafkaListenerContainerFactory"
)
public class MultiTopicKafkaConsumer {

    private final Logger logger = LoggerFactory.getLogger(MultiTopicKafkaConsumer.class);
    private final PersonService personService;
    private final AlienService alienService;

    public MultiTopicKafkaConsumer(PersonService personService, AlienService alienService) {
        this.personService = personService;
        this.alienService = alienService;
    }

    @KafkaHandler
    public void person(Person person) {
        logger.info("Received Person: {}", person.getName());
        personService.createPerson(person)
                .doOnSuccess(saved -> logger.info("Person saved successfully: {}", saved))
                .doOnError(e -> logger.error("Failed to save Person: {}", person.getName(), e))
                .subscribe();
    }

    @KafkaHandler
    public void pet(Pet pet) {
        logger.info("Received Pet: {}", pet.getName());
    }

    @KafkaHandler
    public void alien(Alien alien) {
        logger.info("👽 [Multi] Received Alien: {} ({})", alien.getName(), alien.getColor());
        alienService.createAlien(alien)
                .doOnSuccess(saved -> logger.info("✅ [Multi] Alien saved successfully: {} ({})",
                        saved.getName(), saved.getColor()))
                .doOnError(e -> logger.error("❌ [Multi] Failed to save Alien: {}", alien.getName(), e))
                .subscribe();
    }

    @KafkaHandler(isDefault = true)
    public void unknown(Object object) {
        logger.info("Received unknown: " + object);
    }
}
