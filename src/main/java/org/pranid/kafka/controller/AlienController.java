package org.pranid.kafka.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.pranid.kafka.model.Alien;
import org.pranid.kafka.producer.AlienKafkaProducer;
import org.pranid.kafka.service.AlienService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Alien Management", description = "APIs for managing aliens with Kafka integration. Aliens can be green or purple!")
@RequiredArgsConstructor
@RequestMapping(value = "/alien", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class AlienController {

    private final Logger logger = LoggerFactory.getLogger(AlienController.class);

    private final AlienKafkaProducer alienKafkaProducer;
    private final AlienService alienService;

    @Operation(
            summary = "Get all aliens",
            description = "Retrieves all aliens from MongoDB as a reactive stream"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved all aliens",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Alien.class)
                    )
            )
    })
    @GetMapping("/all")
    public Flux<Alien> getAllAliens() {
        logger.info("👽 Fetching all aliens");
        return alienService.getAllAliens();
    }

    @Operation(
            summary = "Send alien message to Kafka",
            description = "Sends an alien creation message to Kafka 'alien' topic. The consumer will save it to MongoDB. Color must be either 'green' or 'purple'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Alien message sent successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"Alien message sent: Alien(name=Zorg, color=green)\", \"status\": \"success\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid color provided",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"Invalid color. Must be 'green' or 'purple'\", \"status\": \"error\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Failed to send alien message",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"Failed to send alien message: error details\", \"status\": \"failure\"}"
                            )
                    )
            )
    })
    @GetMapping("/send/{name}/{color}")
    public Map<String, Object> sendAlienMessage(
            @Parameter(description = "Alien's name", example = "Zorg", required = true) @PathVariable String name,
            @Parameter(description = "Alien's color (must be 'green' or 'purple')", example = "green", required = true) @PathVariable String color
    ) {
        Map<String, Object> response = new HashMap<>();

        // Validate color
        if (!color.equalsIgnoreCase("green") && !color.equalsIgnoreCase("purple")) {
            response.put("message", "Invalid color. Must be 'green' or 'purple'");
            response.put("status", "error");
            return response;
        }

        Alien alien = new Alien(name, color);

        try {
            alienKafkaProducer.sendAlienMessage(alien);
            response.put("message", "Alien message sent: " + alien);
            response.put("status", "success");
            logger.info("👽 Sent alien message: {} ({})", name, color);
        } catch (Exception e) {
            response.put("message", "Failed to send alien message: " + e.getMessage());
            response.put("status", "failure");
            logger.error("❌ Failed to send alien message", e);
        }

        return response;
    }
}
