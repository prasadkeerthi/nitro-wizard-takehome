package org.example.nitrowizard.client;

import org.example.nitrowizard.config.ApiConfig;
import org.example.nitrowizard.model.Elixir;
import org.example.nitrowizard.dto.ElixirDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WizardWorldClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(WizardWorldClient.class);

    private final RestClient restClient;
    private final ApiConfig config;

    public WizardWorldClient(RestClient restClient, ApiConfig config) {
        this.restClient = restClient;
        this.config = config;
    }

    public List<Elixir> fetchElixirs() throws IOException, InterruptedException {
        List<ElixirDto> dtos = getWithRetries(config.getElixirsPath(),
                new ParameterizedTypeReference<List<ElixirDto>>() {});
        if (dtos == null) {
            return Collections.emptyList();
        }
        List<Elixir> elixirs = new ArrayList<>();
        for (ElixirDto dto : dtos) {
            if (dto == null) {
                continue;
            }
            List<String> ingredientNames = new ArrayList<>();
            if (dto.ingredients != null) {
                for (ElixirDto.IngredientDto ingredientDto : dto.ingredients) {
                    if (ingredientDto != null && ingredientDto.name != null) {
                        ingredientNames.add(ingredientDto.name);
                    }
                }
            }
            elixirs.add(new Elixir(dto.id, dto.name, dto.effect, ingredientNames));
        }
        return elixirs;
    }

    public boolean ping() throws IOException, InterruptedException {
        try {
            ResponseEntity<Void> response = restClient.get()
                    .uri(config.getIngredientsPath())
                    .retrieve()
                    .toBodilessEntity();
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException ex) {
            throw new IOException("API ping failed", ex);
        }
    }

    private <T> T getWithRetries(String path, ParameterizedTypeReference<T> type)
            throws IOException, InterruptedException {
        int attempts = Math.max(0, config.getMaxRetries()) + 1;
        IOException lastException = null;
        for (int i = 0; i < attempts; i++) {
            try {
                return doGet(path, type);
            } catch (IOException ex) {
                lastException = ex;
                if (i == attempts - 1) {
                    throw ex;
                }
                backoff(i);
            }
        }
        throw lastException == null ? new IOException("Failed to fetch data from " + path) : lastException;
    }

    private <T> T doGet(String path, ParameterizedTypeReference<T> type) throws IOException {
        try {
            return restClient.get()
                    .uri(path)
                    .retrieve()
                    .body(type);
        } catch (RestClientException ex) {
            throw new IOException("API request failed for " + path, ex);
        }
    }

    private void backoff(int attemptIndex) throws InterruptedException {
        long delay = Math.max(0L, config.getBackoffMillis()) * (attemptIndex + 1L);
        if (delay <= 0) {
            return;
        }
        LOGGER.info("Retrying API request after {} ms", delay);
        Thread.sleep(delay);
    }
}
