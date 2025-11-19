package org.pranid.kafka.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.pranid.kafka.service.PersonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Health & Info", description = "Health check and application information endpoints")
@RequiredArgsConstructor
@RestController
public class HelloController {

    private final PersonService personService;

    @Operation(
            summary = "Redirect to Swagger UI",
            description = "Redirects the root path to Swagger UI documentation page"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "307", description = "Temporary redirect to Swagger UI")
    })
    @GetMapping("/")
    public Mono<Void> redirectToSwagger(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
        response.getHeaders().setLocation(URI.create("/swagger-ui.html"));
        return response.setComplete();
    }

    @Operation(
            summary = "Health check endpoint",
            description = "Returns a simple JSON response to verify the application is running"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Application is healthy",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"Hello, World!\", \"status\": \"success\"}"
                            )
                    )
            )
    })
    @GetMapping(value = "/hello", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getSampleJson() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello, World!");
        response.put("status", "success");
        return response;
    }
}
