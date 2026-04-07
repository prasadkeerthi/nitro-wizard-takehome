package org.example.nitrowizard.command;

import org.example.nitrowizard.service.ElixirFinderService;
import org.example.nitrowizard.service.ElixirMatchResult;
import org.example.nitrowizard.model.Elixir;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationTest {
    @Test
    void run_shows_help() {
        ElixirFinderService service = mock(ElixirFinderService.class);
        Output output = new Output();

        int code = Application.run(new String[]{"--help"}, service, mockClient(), output.out, output.err);

        assertEquals(0, code);
        assertTrue(output.stdout().contains("Usage: nitro-wizard"));
    }

    @Test
    void run_missing_ingredients_value() {
        ElixirFinderService service = mock(ElixirFinderService.class);
        Output output = new Output();

        int code = Application.run(new String[]{"--ingredients"}, service, mockClient(), output.out, output.err);

        assertEquals(2, code);
        assertTrue(output.stderr().contains("Missing value for --ingredients."));
    }

    @Test
    void run_unknown_argument() {
        ElixirFinderService service = mock(ElixirFinderService.class);
        Output output = new Output();

        int code = Application.run(new String[]{"--bogus"}, service, mockClient(), output.out, output.err);

        assertEquals(2, code);
        assertTrue(output.stderr().contains("Unknown argument"));
    }


    @Test
    void run_requires_non_empty_ingredients() {
        ElixirFinderService service = mock(ElixirFinderService.class);
        Output output = new Output();

        int code = Application.run(new String[]{"--ingredients", "   "}, service, mockClient(), output.out, output.err);

        assertEquals(2, code);
        assertTrue(output.stderr().contains("No ingredients provided."));
    }

    @Test
    void run_rejects_invalid_characters() {
        ElixirFinderService service = mock(ElixirFinderService.class);
        Output output = new Output();

        int code = Application.run(new String[]{"--ingredients", "Boomslang@Skin"}, service, mockClient(),
                output.out, output.err);

        assertEquals(2, code);
        assertTrue(output.stderr().contains("invalid characters"));
    }

    @Test
    void run_prints_matches() throws IOException, InterruptedException {
        ElixirFinderService service = mock(ElixirFinderService.class);
        when(service.findElixirs(List.of("Leech Juice", "Boomslang Skin")))
                .thenReturn(new ElixirMatchResult(
                        List.of(new Elixir("1", "Polyjuice", "Transform", List.of("Leech Juice"))),
                        5));
        Output output = new Output();

        int code = Application.run(new String[]{"--ingredients", "Leech Juice, Boomslang Skin"},
                service, mockClient(), output.out, output.err);

        assertEquals(0, code);
        assertTrue(output.stdout().contains("Elixirs you can brew:"));
        assertTrue(output.stdout().contains("- Polyjuice"));
    }

    @Test
    void run_json_output() throws IOException, InterruptedException {
        ElixirFinderService service = mock(ElixirFinderService.class);
        when(service.findElixirs(List.of("Leech Juice")))
                .thenReturn(new ElixirMatchResult(
                        List.of(new Elixir("1", "Polyjuice", "Transform", List.of("Leech Juice"))),
                        10));
        Output output = new Output();

        int code = Application.run(new String[]{"--ingredients", "Leech Juice", "--output", "json"},
                service, mockClient(), output.out, output.err);

        assertEquals(0, code);
        assertTrue(output.stdout().contains("\"elixirs\""));
        assertTrue(output.stdout().contains("\"Polyjuice\""));
    }

    @Test
    void run_sample_outputs_elixir() throws IOException, InterruptedException {
        ElixirFinderService service = mock(ElixirFinderService.class);
        Output output = new Output();
        var client = mockClient();
        when(client.fetchElixirs()).thenReturn(List.of(
                new Elixir("1", "Polyjuice", "Transform", List.of("Leech Juice"))));

        int code = Application.run(new String[]{"--sample"}, service, client, output.out, output.err);

        assertEquals(0, code);
        assertTrue(output.stdout().contains("Sample elixir:"));
        assertTrue(output.stdout().contains("Polyjuice"));
    }

    @Test
    void run_list_elixirs_outputs_names() throws IOException, InterruptedException {
        ElixirFinderService service = mock(ElixirFinderService.class);
        Output output = new Output();
        var client = mockClient();
        when(client.fetchElixirs()).thenReturn(List.of(
                new Elixir("1", "Polyjuice", "Transform", List.of("Leech Juice")),
                new Elixir("2", "Felix Felicis", "Luck", List.of("Ashwinder Egg"))));

        int code = Application.run(new String[]{"--list-elixirs"}, service, client, output.out, output.err);

        assertEquals(0, code);
        assertTrue(output.stdout().contains("Elixirs:"));
        assertTrue(output.stdout().contains("Polyjuice"));
        assertTrue(output.stdout().contains("Felix Felicis"));
    }

    @Test
    void run_list_elixirs_full_outputs_details() throws IOException, InterruptedException {
        ElixirFinderService service = mock(ElixirFinderService.class);
        Output output = new Output();
        var client = mockClient();
        when(client.fetchElixirs()).thenReturn(List.of(
                new Elixir("1", "Polyjuice", "Transform", List.of("Leech Juice"))));

        int code = Application.run(new String[]{"--list-elixirs=full"}, service, client, output.out, output.err);

        assertEquals(0, code);
        assertTrue(output.stdout().contains("Effect: Transform"));
        assertTrue(output.stdout().contains("Ingredients: Leech Juice"));
    }

    private static final class Output {
        private final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        private final ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
        private final PrintStream out = new PrintStream(outBuffer);
        private final PrintStream err = new PrintStream(errBuffer);

        private String stdout() {
            return outBuffer.toString();
        }

        private String stderr() {
            return errBuffer.toString();
        }
    }

    private static org.example.nitrowizard.client.WizardWorldClient mockClient() {
        return mock(org.example.nitrowizard.client.WizardWorldClient.class);
    }
}
