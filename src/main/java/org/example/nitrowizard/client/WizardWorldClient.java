package org.example.nitrowizard.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nitrowizard.config.ApiConfig;
import org.example.nitrowizard.model.Elixir;
import org.example.nitrowizard.model.Ingredient;
import org.example.nitrowizard.dto.ElixirDto;
import org.example.nitrowizard.dto.IngredientDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WizardWorldClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(WizardWorldClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ApiConfig config;

    public WizardWorldClient(HttpClient httpClient, ObjectMapper objectMapper, ApiConfig config) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.config = config;
    }

    public static WizardWorldClient createDefault() {
        ApiConfig config = ApiConfig.fromEnv();
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(config.getConnectTimeout())
                .build();
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new WizardWorldClient(client, mapper, config);
    }

    public List<Ingredient> fetchIngredients() throws IOException, InterruptedException {
        List<IngredientDto> dtos = getWithRetries(buildUri(config.getIngredientsPath()),
                new TypeReference<List<IngredientDto>>() {
                });
        if (dtos == null) {
            return Collections.emptyList();
        }
        List<Ingredient> ingredients = new ArrayList<>();
        for (IngredientDto dto : dtos) {
            if (dto == null) {
                continue;
            }
            ingredients.add(new Ingredient(dto.id, dto.name));
        }
        return ingredients;
    }

    public List<Elixir> fetchElixirs() throws IOException, InterruptedException {
        List<ElixirDto> dtos = getWithRetries(buildUri(config.getElixirsPath()),
                new TypeReference<List<ElixirDto>>() {
                });
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
                for (IngredientDto ingredientDto : dto.ingredients) {
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
        URI uri = buildUri(config.getIngredientsPath());
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(config.getRequestTimeout())
                .GET()
                .build();
        HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    private <T> T getWithRetries(URI uri, TypeReference<T> type) throws IOException, InterruptedException {
        int attempts = Math.max(0, config.getMaxRetries()) + 1;
        IOException lastException = null;
        for (int i = 0; i < attempts; i++) {
            try {
                return doGet(uri, type);
            } catch (IOException ex) {
                lastException = ex;
                if (i == attempts - 1) {
                    throw ex;
                }
                backoff(i);
            }
        }
        throw lastException == null ? new IOException("Failed to fetch data from " + uri) : lastException;
    }

    private <T> T doGet(URI uri, TypeReference<T> type) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(config.getRequestTimeout())
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String body = response.body();
            String snippet = body == null ? "" : body.substring(0, Math.min(body.length(), 200));
            throw new IOException("API request failed (" + response.statusCode() + ") for " + uri + ": " + snippet);
        }
        return objectMapper.readValue(response.body(), type);
    }

    private void backoff(int attemptIndex) throws InterruptedException {
        long delay = Math.max(0L, config.getBackoffMillis()) * (attemptIndex + 1L);
        if (delay <= 0) {
            return;
        }
        LOGGER.info("Retrying API request after {} ms", delay);
        Thread.sleep(delay);
    }

    private URI buildUri(String path) {
        String base = config.getBaseUrl();
        String normalizedBase = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return URI.create(normalizedBase + normalizedPath);
    }

}
