package org.example.nitrowizard.service;

import org.example.nitrowizard.config.SynonymConfig;
import org.example.nitrowizard.model.Elixir;
import org.example.nitrowizard.client.WizardWorldClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElixirFinderServiceTest {
    @Test
    void findElixirs_matches_required_ingredients() throws IOException, InterruptedException {
        WizardWorldClient client = mock(WizardWorldClient.class);
        Elixir matching = new Elixir("1", "Polyjuice", "Transform", List.of("Boomslang Skin", "Leech Juice"));
        Elixir nonMatching = new Elixir("2", "Felix Felicis", "Luck", List.of("Ashwinder Egg"));
        when(client.fetchElixirs()).thenReturn(List.of(nonMatching, matching));

        ElixirFinderService service = new ElixirFinderService(client, new SynonymConfig());
        ElixirMatchResult result = service.findElixirs(List.of("Leech Juice", "Boomslang Skin"));

        assertEquals(1, result.getMatches().size());
        assertEquals("Polyjuice", result.getMatches().get(0).getName());
        assertEquals(2, result.getTotalElixirs());
    }

    @Test
    void findElixirs_returns_empty_when_no_available_ingredients() throws IOException, InterruptedException {
        WizardWorldClient client = mock(WizardWorldClient.class);
        ElixirFinderService service = new ElixirFinderService(client, new SynonymConfig());

        ElixirMatchResult result = service.findElixirs(List.of());

        assertEquals(0, result.getMatches().size());
        assertEquals(0, result.getTotalElixirs());
        verifyNoInteractions(client);
    }
}
