package org.example.nitrowizard;

import org.example.nitrowizard.client.WizardWorldClient;
import org.example.nitrowizard.model.Elixir;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WizardWorldClient wizardWorldClient;

    @Test
    void match_flow_works() throws Exception {
        when(wizardWorldClient.fetchElixirs())
                .thenReturn(List.of(new Elixir("1", "Polyjuice", "Transform", List.of("Leech Juice"))));

        mockMvc.perform(post("/api/elixirs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ingredients\":[\"Leech Juice\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchCount").value(1))
                .andExpect(jsonPath("$.elixirs[0].name").value("Polyjuice"));
    }

    @Test
    void list_flow_works() throws Exception {
        when(wizardWorldClient.fetchElixirs())
                .thenReturn(List.of(new Elixir("1", "Polyjuice", "Transform", List.of("Leech Juice"))));

        mockMvc.perform(get("/api/elixirs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Polyjuice"));
    }
}
