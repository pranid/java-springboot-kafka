package org.pranid.kafka.consumer;

import org.pranid.kafka.model.Alien;
import org.pranid.kafka.service.AlienService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AlienKafkaConsumer {

    private final Logger logger = LoggerFactory.getLogger(AlienKafkaConsumer.class);
    private final AlienService alienService;

    public AlienKafkaConsumer(AlienService alienService) {
        this.alienService = alienService;
    }

    @KafkaListener(id = "alienConsumer", topics = "alien")
    public void listenAlien(Alien alien) {
        logger.info("👽 Received Alien: {} ({})", alien.getName(), alien.getColor());

        alienService.createAlien(alien)
                .doOnSuccess(saved -> logger.info("✅ Alien saved successfully: {} ({})",
                        saved.getName(), saved.getColor()))
                .doOnError(e -> logger.error("❌ Failed to save Alien: {}", alien.getName(), e))
                .subscribe();
    }
}
