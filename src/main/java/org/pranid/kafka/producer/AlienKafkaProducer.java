package org.pranid.kafka.producer;

import org.pranid.kafka.model.Alien;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlienKafkaProducer {

    private final Logger logger = LoggerFactory.getLogger(AlienKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AlienKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAlienMessage(Alien alien) {
        kafkaTemplate.send("alien", alien);
        logger.info("👽 Sent Kafka message for alien: {} ({})", alien.getName(), alien.getColor());
    }
}
