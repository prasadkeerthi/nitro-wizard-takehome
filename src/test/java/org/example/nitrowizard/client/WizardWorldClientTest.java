package org.example.nitrowizard.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nitrowizard.config.ApiConfig;
import org.example.nitrowizard.model.Elixir;
import org.example.nitrowizard.model.Ingredient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WizardWorldClientTest {
    @Test
    void fetchElixirs_ignores_unknown_fields() throws IOException, InterruptedException {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("""
                [
                  {
                    "id":"1",
                    "name":"Polyjuice",
                    "effect":"Transform",
                    "sideEffects":"Nausea",
                    "ingredients":[{"id":"i1","name":"Leech Juice"}]
                  }
                ]
                """);
        when(httpClient.send(ArgumentMatchers.any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        WizardWorldClient client = new WizardWorldClient(
                httpClient,
                new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false),
                testConfig(0, 0));

        List<Elixir> elixirs = client.fetchElixirs();

        assertEquals(1, elixirs.size());
        assertEquals("Polyjuice", elixirs.get(0).getName());
        assertEquals(List.of("Leech Juice"), elixirs.get(0).getIngredients());
    }

    @Test
    void fetchIngredients_parses_list() throws IOException, InterruptedException {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("""
                [
                  {"id":"a1","name":"Boomslang Skin"},
                  {"id":"a2","name":"Leech Juice"}
                ]
                """);
        when(httpClient.send(ArgumentMatchers.any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        WizardWorldClient client = new WizardWorldClient(
                httpClient,
                new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false),
                testConfig(0, 0));

        List<Ingredient> ingredients = client.fetchIngredients();

        assertEquals(2, ingredients.size());
        assertEquals("Boomslang Skin", ingredients.get(0).getName());
    }

    @Test
    void fetchElixirs_retries_on_io_exception() throws IOException, InterruptedException {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("[]");
        when(httpClient.send(ArgumentMatchers.any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new IOException("Transient failure"))
                .thenReturn(response);

        WizardWorldClient client = new WizardWorldClient(
                httpClient,
                new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false),
                testConfig(1, 0));

        List<Elixir> elixirs = client.fetchElixirs();

        assertEquals(0, elixirs.size());
        verify(httpClient, times(2))
                .send(ArgumentMatchers.any(HttpRequest.class),
                        ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void fetchElixirs_throws_on_non_2xx() throws IOException, InterruptedException {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(500);
        when(response.body()).thenReturn("Server error");
        when(httpClient.send(ArgumentMatchers.any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        WizardWorldClient client = new WizardWorldClient(
                httpClient,
                new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false),
                testConfig(0, 0));

        assertThrows(IOException.class, client::fetchElixirs);
    }

    @Test
    void ping_returns_true_on_2xx() throws IOException, InterruptedException {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<Void> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(204);
        when(httpClient.send(ArgumentMatchers.any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<Void>>any()))
                .thenReturn(response);

        WizardWorldClient client = new WizardWorldClient(
                httpClient,
                new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false),
                testConfig(0, 0));

        assertEquals(true, client.ping());
    }

    private static ApiConfig testConfig(int maxRetries, long backoffMillis) {
        return new ApiConfig(
                "http://localhost",
                "/Ingredients",
                "/Elixirs",
                Duration.ofSeconds(1),
                Duration.ofSeconds(1),
                maxRetries,
                backoffMillis);
    }
}
