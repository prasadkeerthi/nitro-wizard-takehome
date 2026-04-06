package org.example.nitrowizard.service;

import org.example.nitrowizard.config.SynonymConfig;
import org.example.nitrowizard.model.Elixir;
import org.example.nitrowizard.client.WizardWorldClient;
import org.example.nitrowizard.util.IngredientNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ElixirFinderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElixirFinderService.class);
    private final WizardWorldClient client;
    private final SynonymConfig synonymConfig;

    public ElixirFinderService(WizardWorldClient client, SynonymConfig synonymConfig) {
        this.client = client;
        this.synonymConfig = synonymConfig;
    }

    public ElixirMatchResult findElixirs(List<String> availableIngredients) throws IOException, InterruptedException {
        Set<String> availableSet = normalizeAvailable(availableIngredients);
        if (availableSet.isEmpty()) {
            LOGGER.debug("No available ingredients after normalization.");
            return new ElixirMatchResult(List.of(), 0);
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
        LOGGER.info("Matched {} elixir(s) out of {}.", matches.size(), elixirs.size());
        return new ElixirMatchResult(matches, elixirs.size());
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
