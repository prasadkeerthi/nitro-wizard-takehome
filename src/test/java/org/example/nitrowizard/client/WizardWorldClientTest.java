package org.example.nitrowizard.client;

import org.example.nitrowizard.config.ApiConfig;
import org.example.nitrowizard.model.Elixir;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WizardWorldClientTest {
    @Test
    void fetchElixirs_ignores_unknown_fields() throws IOException, InterruptedException {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer server = bindTo(builder).build();
        server.expect(once(), requestTo("http://localhost/Elixirs"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [
                          {
                            "id":"1",
                            "name":"Polyjuice",
                            "effect":"Transform",
                            "sideEffects":"Nausea",
                            "ingredients":[{"id":"i1","name":"Leech Juice"}]
                          }
                        ]
                        """, MediaType.APPLICATION_JSON));

        WizardWorldClient client = new WizardWorldClient(builder.build(), testConfig(0, 0));

        List<Elixir> elixirs = client.fetchElixirs();

        assertEquals(1, elixirs.size());
        assertEquals("Polyjuice", elixirs.get(0).getName());
        assertEquals(List.of("Leech Juice"), elixirs.get(0).getIngredients());
        server.verify();
    }

    @Test
    void fetchElixirs_retries_on_io_exception() throws IOException, InterruptedException {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer server = bindTo(builder).build();
        server.expect(once(), requestTo("http://localhost/Elixirs"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());
        server.expect(once(), requestTo("http://localhost/Elixirs"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        WizardWorldClient client = new WizardWorldClient(builder.build(), testConfig(1, 0));

        List<Elixir> elixirs = client.fetchElixirs();

        assertEquals(0, elixirs.size());
        server.verify();
    }

    @Test
    void fetchElixirs_throws_on_non_2xx() throws IOException, InterruptedException {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer server = bindTo(builder).build();
        server.expect(once(), requestTo("http://localhost/Elixirs"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        WizardWorldClient client = new WizardWorldClient(builder.build(), testConfig(0, 0));

        assertThrows(IOException.class, client::fetchElixirs);
        server.verify();
    }

    @Test
    void ping_returns_true_on_2xx() throws IOException, InterruptedException {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer server = bindTo(builder).build();
        server.expect(once(), requestTo("http://localhost/Ingredients"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());

        WizardWorldClient client = new WizardWorldClient(builder.build(), testConfig(0, 0));

        assertEquals(true, client.ping());
        server.verify();
    }

    private static ApiConfig testConfig(int maxRetries, long backoffMillis) {
        ApiConfig config = new ApiConfig();
        config.setBaseUrl("http://localhost");
        config.setIngredientsPath("/Ingredients");
        config.setElixirsPath("/Elixirs");
        config.setConnectTimeout(Duration.ofSeconds(1));
        config.setRequestTimeout(Duration.ofSeconds(1));
        config.setMaxRetries(maxRetries);
        config.setBackoffMillis(backoffMillis);
        return config;
    }
}
