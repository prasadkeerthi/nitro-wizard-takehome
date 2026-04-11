package org.example.nitrowizard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nitrowizard.client.WizardWorldClient;
import org.example.nitrowizard.dto.MatchRequest;
import org.example.nitrowizard.model.Elixir;
import org.example.nitrowizard.service.ElixirFinderService;
import org.example.nitrowizard.service.ElixirMatchResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ElixirController.class)
class ElixirControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ElixirFinderService elixirFinderService;

    @MockBean
    private WizardWorldClient wizardWorldClient;

    @TestConfiguration
    static class MetricsConfig {
        @Bean
        io.micrometer.core.instrument.MeterRegistry meterRegistry() {
            return new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        }
    }

    @Test
    void matchElixirs_returns_matches() throws Exception {
        List<String> ingredients = List.of("Boomslang Skin", "Leech Juice");
        Elixir elixir = new Elixir("1", "Polyjuice", "Transform", List.of("Boomslang Skin", "Leech Juice"));
        when(elixirFinderService.findElixirs(ingredients))
                .thenReturn(new ElixirMatchResult(List.of(elixir)));

        mockMvc.perform(post("/api/elixirs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MatchRequest(ingredients))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchCount").value(1))
                .andExpect(jsonPath("$.elixirs[0].name").value("Polyjuice"))
                .andExpect(jsonPath("$.elixirs[0].id").doesNotExist());
    }

    @Test
    void matchElixirs_rejects_empty_ingredients() throws Exception {
        mockMvc.perform(post("/api/elixirs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MatchRequest(List.of()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sampleElixir_returns_sample() throws Exception {
        when(wizardWorldClient.fetchElixirs())
                .thenReturn(List.of(new Elixir("1", "Polyjuice", "Transform", List.of("Leech Juice"))));

        mockMvc.perform(get("/api/elixirs/sample"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Polyjuice"));
    }

    @Test
    void listElixirs_returns_names_only() throws Exception {
        when(wizardWorldClient.fetchElixirs())
                .thenReturn(List.of(
                        new Elixir("1", "Polyjuice", "Transform", List.of("Leech Juice")),
                        new Elixir("2", "Felix Felicis", "Luck", List.of("Ashwinder Egg"))));

        mockMvc.perform(get("/api/elixirs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Polyjuice"))
                .andExpect(jsonPath("$[1]").value("Felix Felicis"));
    }

    @Test
    void listElixirs_full_returns_objects() throws Exception {
        when(wizardWorldClient.fetchElixirs())
                .thenReturn(List.of(
                        new Elixir("1", "Polyjuice", "Transform", List.of("Leech Juice"))));

        mockMvc.perform(get("/api/elixirs").param("full", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Polyjuice"))
                .andExpect(jsonPath("$[0].ingredients[0]").value("Leech Juice"));
    }

    @Test
    void ping_returns_ok() throws Exception {
        when(wizardWorldClient.ping()).thenReturn(true);

        mockMvc.perform(get("/api/health/ping"))
                .andExpect(status().isOk());
    }
}
