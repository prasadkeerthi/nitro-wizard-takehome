package org.example.nitrowizard.service;

import org.example.nitrowizard.config.SynonymConfig;
import org.example.nitrowizard.model.Elixir;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.example.nitrowizard.client.WizardWorldClient;
import org.example.nitrowizard.util.IngredientNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ElixirFinderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElixirFinderService.class);
    private final WizardWorldClient client;
    private final SynonymConfig synonymConfig;
    private final Counter matchRequests;

    public ElixirFinderService(WizardWorldClient client, SynonymConfig synonymConfig, MeterRegistry meterRegistry) {
        this.client = client;
        this.synonymConfig = synonymConfig;
        this.matchRequests = Counter.builder("nitro_wizard.elixir_match_requests")
                .description("Number of elixir match requests")
                .register(meterRegistry);
    }

    public ElixirMatchResult findElixirs(List<String> availableIngredients) throws IOException, InterruptedException {
        matchRequests.increment();
        Set<String> availableSet = normalizeAvailable(availableIngredients);
        if (availableSet.isEmpty()) {
            LOGGER.debug("No available ingredients after normalization.");
            return new ElixirMatchResult(List.of());
        }

        List<Elixir> elixirs = client.fetchElixirs();
        List<Elixir> matches = new ArrayList<>();
        for (Elixir elixir : elixirs) {
            if (elixir == null) {
                continue;
            }
            if (canBrew(elixir, availableSet)) {
                matches.add(elixir);
            }
        }

        matches.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        LOGGER.info("Matched {} elixir(s).", matches.size());
        return new ElixirMatchResult(matches);
    }

    private Set<String> normalizeAvailable(List<String> availableIngredients) {
        Set<String> availableSet = new HashSet<>();
        if (availableIngredients == null) {
            return availableSet;
        }
        for (String ingredient : availableIngredients) {
            String normalized = IngredientNormalizer.normalize(ingredient, synonymConfig);
            if (!normalized.isEmpty()) {
                availableSet.add(normalized);
            }
        }
        return availableSet;
    }

    private boolean canBrew(Elixir elixir, Set<String> availableSet) {
        List<String> required = elixir.getIngredients();
        if (required.isEmpty()) {
            LOGGER.debug("Skipping elixir with no ingredient list: {}", elixir.getName());
            return false;
        }
        for (String ingredient : required) {
            String normalized = IngredientNormalizer.normalize(ingredient, synonymConfig);
            if (normalized.isEmpty() || !availableSet.contains(normalized)) {
                return false;
            }
        }
        return true;
    }
}
