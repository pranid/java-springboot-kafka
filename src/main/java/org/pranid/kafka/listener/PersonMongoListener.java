package org.pranid.kafka.listener;

import org.pranid.kafka.model.Person;
import org.pranid.kafka.producer.PersonKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;

@Component
public class PersonMongoListener extends AbstractMongoEventListener<Person> {
    private final PersonKafkaProducer kafkaProducer;
    private final Logger logger = LoggerFactory.getLogger(PersonMongoListener.class);

    @Autowired
    public PersonMongoListener(PersonKafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void onAfterSave(AfterSaveEvent<Person> event) {
        Person person = event.getSource();
        // if (person.getId() == null) return; // skip updates if needed
        kafkaProducer.sendPersonCreatedEvent(person);
        logger.info("📤 After save message: {}", person.getName());
    }
}
