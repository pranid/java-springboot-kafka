package org.pranid.kafka.producer;

import org.pranid.kafka.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PersonKafkaProducer {

    private final Logger logger = LoggerFactory.getLogger(PersonKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PersonKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPersonCreatedEvent(Person person) {
        kafkaTemplate.send("person.created", person);
        logger.info("📤 Sent Kafka message for new person: {}", person.getName());
    }
}
