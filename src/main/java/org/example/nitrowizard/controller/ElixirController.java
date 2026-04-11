package org.example.nitrowizard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.nitrowizard.client.WizardWorldClient;
import org.example.nitrowizard.dto.MatchRequest;
import org.example.nitrowizard.dto.MatchResponse;
import org.example.nitrowizard.dto.MatchResponseElixir;
import org.example.nitrowizard.model.Elixir;
import org.example.nitrowizard.service.ElixirFinderService;
import org.example.nitrowizard.service.ElixirMatchResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.example.nitrowizard.util.InputValidator;
import org.example.nitrowizard.util.ValidationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Elixirs", description = "Elixir matching and catalog APIs")
public class ElixirController {
    private final ElixirFinderService elixirFinderService;
    private final WizardWorldClient wizardWorldClient;
    private final Counter sampleRequests;
    private final Counter listRequests;
    private final Counter pingRequests;

    public ElixirController(ElixirFinderService elixirFinderService,
                            WizardWorldClient wizardWorldClient,
                            MeterRegistry meterRegistry) {
        this.elixirFinderService = elixirFinderService;
        this.wizardWorldClient = wizardWorldClient;
        this.sampleRequests = Counter.builder("nitro_wizard.elixir_sample_requests")
                .description("Number of sample elixir requests")
                .register(meterRegistry);
        this.listRequests = Counter.builder("nitro_wizard.elixir_list_requests")
                .description("Number of list elixirs requests")
                .register(meterRegistry);
        this.pingRequests = Counter.builder("nitro_wizard.ping_requests")
                .description("Number of health ping requests")
                .register(meterRegistry);
    }

    @PostMapping("/elixirs/match")
    @Operation(summary = "Find elixirs that can be brewed from the provided ingredients")
    public MatchResponse matchElixirs(@Valid @org.springframework.web.bind.annotation.RequestBody MatchRequest request) {
        ValidationResult validation = InputValidator.validateIngredients(request.ingredients());
        if (!validation.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validation.errorMessage());
        }
        try {
            ElixirMatchResult result = elixirFinderService.findElixirs(validation.cleanedIngredients());
            List<MatchResponseElixir> matches = result.getMatches().stream()
                    .map(elixir -> new MatchResponseElixir(
                            elixir.getName(),
                            elixir.getEffect(),
                            elixir.getIngredients()))
                    .toList();
            return new MatchResponse(matches.size(), matches);
        } catch (IOException | InterruptedException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch elixirs", ex);
        }
    }

    @GetMapping("/elixirs/sample")
    @Operation(summary = "Get a sample elixir with ingredients")
    public Elixir sampleElixir() {
        sampleRequests.increment();
        try {
            List<Elixir> elixirs = wizardWorldClient.fetchElixirs();
            for (Elixir elixir : elixirs) {
                if (elixir != null && !elixir.getName().isEmpty() && !elixir.getIngredients().isEmpty()) {
                    return elixir;
                }
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No sample elixir available");
        } catch (IOException | InterruptedException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch sample elixir", ex);
        }
    }

    @GetMapping("/elixirs")
    @Operation(summary = "List all elixirs, optionally with details")
    public ResponseEntity<?> listElixirs(@RequestParam(name = "full", defaultValue = "false") boolean full) {
        listRequests.increment();
        try {
            List<Elixir> elixirs = wizardWorldClient.fetchElixirs();
            if (full) {
                return ResponseEntity.ok(elixirs);
            }
            List<String> names = elixirs.stream()
                    .filter(elixir -> elixir != null && !elixir.getName().isEmpty())
                    .map(Elixir::getName)
                    .toList();
            return ResponseEntity.ok(names);
        } catch (IOException | InterruptedException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch elixirs", ex);
        }
    }

    @GetMapping("/health/ping")
    @Operation(summary = "Check API reachability")
    public ResponseEntity<String> ping() {
        pingRequests.increment();
        try {
            boolean ok = wizardWorldClient.ping();
            if (ok) {
                return ResponseEntity.ok("OK");
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("API ping failed");
        } catch (IOException | InterruptedException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "API ping failed", ex);
        }
    }
}
