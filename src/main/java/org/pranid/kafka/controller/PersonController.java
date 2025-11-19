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
import org.pranid.kafka.model.Person;
import org.pranid.kafka.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Tag(name = "Person Management", description = "APIs for managing persons with Kafka integration")
@RequiredArgsConstructor
@RequestMapping(value = "/person", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class PersonController {

    private final Logger logger = LoggerFactory.getLogger(PersonController.class);

    private final KafkaTemplate<Object, Object> template;

    private final PersonService personService;

    @Operation(
            summary = "Get all persons",
            description = "Retrieves all persons from MongoDB as a reactive stream"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved all persons",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Person.class)
                    )
            )
    })
    @GetMapping("all")
    public Flux<Person> getAll() {
        return personService.getAllPersons();
    }

    @Operation(
            summary = "Create person via Kafka",
            description = "Sends a person creation message to Kafka 'person' topic. The consumer will save it to MongoDB."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Person message sent successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"Sent: Person(name=John, gender=male, age=25)\", \"status\": \"success\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Failed to send person message",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"Failed to send message: error details\", \"status\": \"failure\"}"
                            )
                    )
            )
    })
    @GetMapping(path = "/create-person/{name}/{gender}/{age}")
    public Map<String, Object> createPerson(
            @Parameter(description = "Person's name", example = "John") @PathVariable String name,
            @Parameter(description = "Person's gender", example = "male") @PathVariable String gender,
            @Parameter(description = "Person's age", example = "25") @PathVariable int age
    ) {
        Map<String, Object> response = new HashMap<>();
        Person person = new Person(name, gender, age);

        CompletableFuture<SendResult<Object, Object>> future = template.send("person", person);

        try {
            SendResult<Object, Object> result = future.get();
            response.put("message", "Sent: " + person);
            response.put("status", "success");
        } catch (Exception e) {
            response.put("message", "Failed to send message: " + e.getMessage());
            response.put("status", "failure");
        }

        return response;
    }

    @Operation(
            summary = "Bulk create persons",
            description = "Creates multiple persons directly in MongoDB (specified count). Useful for testing and data seeding."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bulk creation completed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"Inserted 100 persons\", \"successCount\": 100, \"failureCount\": 0}"
                            )
                    )
            )
    })
    @GetMapping(path = "/bulk-create-persons/{personCount}")
    public Map<String, Object> bulkCreatePersons(
            @Parameter(description = "Number of persons to create", example = "100") @PathVariable int personCount
    ) {
        logger.info("Bulk creating {} persons...", personCount);
        Map<String, Object> response = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;

        for (int i = 1; i <= personCount; i++) {
            Person person = new Person("Person_" + i, "gender_" + i, 20 + (i % 30));
            try {
                personService.createPerson(person)
                        .doOnSuccess(saved -> logger.info("Person saved successfully: {}", saved))
                        .doOnError(e -> logger.error("Failed to save Person: {}", person.getName(), e))
                        .subscribe();
                successCount++;
            } catch (Exception e) {
                failureCount++;
            }
        }

        response.put("message", "Inserted 100 persons");
        response.put("successCount", successCount);
        response.put("failureCount", failureCount);
        return response;
    }
}
