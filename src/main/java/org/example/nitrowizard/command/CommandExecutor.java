package org.example.nitrowizard.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nitrowizard.client.WizardWorldClient;
import org.example.nitrowizard.model.Elixir;
import org.example.nitrowizard.service.ElixirFinderService;
import org.example.nitrowizard.service.ElixirMatchResult;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public final class CommandExecutor {
    private CommandExecutor() {
    }

    public static int execute(CommandOptions options,
                              ElixirFinderService service,
                              WizardWorldClient client,
                              PrintStream out,
                              PrintStream err,
                              Logger logger) {
        return switch (options.mode()) {
            case PING -> ping(client, out, err, logger);
            case SAMPLE -> printSample(client, options.outputMode(), out, err, logger);
            case LIST -> printElixirs(client, options.outputMode(), options.listElixirsFull(), out, err, logger);
            case MATCH -> matchElixirs(options, service, out, err, logger);
        };
    }

    private static int ping(WizardWorldClient client, PrintStream out, PrintStream err, Logger logger) {
        try {
            boolean ok = client.ping();
            if (ok) {
                out.println("OK");
                return 0;
            }
            err.println("API ping failed.");
            return 1;
        } catch (IOException | InterruptedException ex) {
            logger.error("API ping failed.", ex);
            err.println("API ping failed: " + ex.getMessage());
            return 1;
        }
    }

    private static int matchElixirs(CommandOptions options,
                                    ElixirFinderService service,
                                    PrintStream out,
                                    PrintStream err,
                                    Logger logger) {
        try {
            logger.info("Searching elixirs for {} ingredient(s).", options.ingredients().size());
            ElixirMatchResult result = service.findElixirs(options.ingredients());
            List<Elixir> matches = result.getMatches();
            if (options.outputMode() == OutputMode.JSON) {
                writeJson(out, options.ingredients(), result, options.verbose());
                return 0;
            }

            if (matches.isEmpty()) {
                out.println("No elixirs can be brewed with the provided ingredients.");
                logger.info("No elixirs matched the provided ingredients.");
                return 0;
            }
            out.println("Elixirs you can brew:");
            for (Elixir elixir : matches) {
                out.println("- " + elixir.getName());
            }
            if (options.verbose()) {
                out.println();
                out.println("Diagnostics:");
                out.println("  Input ingredients: " + options.ingredients().size());
                out.println("  Total elixirs fetched: " + result.getTotalElixirs());
                out.println("  Matches: " + matches.size());
            }
            return 0;
        } catch (IOException | InterruptedException ex) {
            logger.error("Failed to fetch elixirs.", ex);
            err.println("Failed to fetch elixirs: " + ex.getMessage());
            return 1;
        }
    }

    private static void writeJson(PrintStream out,
                                  List<String> ingredients,
                                  ElixirMatchResult result,
                                  boolean verbose) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonOutput payload = new JsonOutput(ingredients, result.getMatches(), result.getTotalElixirs(),
                result.getMatches().size(), verbose);
        out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload));
    }

    private static int printSample(WizardWorldClient client,
                                   OutputMode outputMode,
                                   PrintStream out,
                                   PrintStream err,
                                   Logger logger) {
        try {
            List<Elixir> elixirs = client.fetchElixirs();
            Elixir sample = findSample(elixirs);
            if (sample == null) {
                err.println("No sample elixir available.");
                return 1;
            }
            if (outputMode == OutputMode.JSON) {
                ObjectMapper mapper = new ObjectMapper();
                out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(sample));
                return 0;
            }
            out.println("Sample elixir:");
            out.println("Name: " + sample.getName());
            out.println("Ingredients: " + String.join(", ", sample.getIngredients()));
            return 0;
        } catch (IOException | InterruptedException ex) {
            logger.error("Failed to fetch sample elixir.", ex);
            err.println("Failed to fetch sample elixir: " + ex.getMessage());
            return 1;
        }
    }

    private static Elixir findSample(List<Elixir> elixirs) {
        if (elixirs == null) {
            return null;
        }
        for (Elixir elixir : elixirs) {
            if (elixir == null) {
                continue;
            }
            if (!elixir.getName().isEmpty() && !elixir.getIngredients().isEmpty()) {
                return elixir;
            }
        }
        return null;
    }

    private static int printElixirs(WizardWorldClient client,
                                    OutputMode outputMode,
                                    boolean full,
                                    PrintStream out,
                                    PrintStream err,
                                    Logger logger) {
        try {
            List<Elixir> elixirs = client.fetchElixirs();
            if (outputMode == OutputMode.JSON) {
                ObjectMapper mapper = new ObjectMapper();
                out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(elixirs));
                return 0;
            }
            if (elixirs.isEmpty()) {
                out.println("No elixirs found.");
                return 0;
            }
            out.println("Elixirs:");
            for (Elixir elixir : elixirs) {
                if (elixir == null || elixir.getName().isEmpty()) {
                    continue;
                }
                if (!full) {
                    out.println("- " + elixir.getName());
                    continue;
                }
                String effect = elixir.getEffect().isEmpty() ? "Unknown effect" : elixir.getEffect();
                String ingredients = elixir.getIngredients().isEmpty()
                        ? "Unknown ingredients"
                        : String.join(", ", elixir.getIngredients());
                out.println("- " + elixir.getName());
                out.println("  Effect: " + effect);
                out.println("  Ingredients: " + ingredients);
            }
            return 0;
        } catch (IOException | InterruptedException ex) {
            logger.error("Failed to list elixirs.", ex);
            err.println("Failed to list elixirs: " + ex.getMessage());
            return 1;
        }
    }

    private record JsonOutput(List<String> ingredients,
                              int inputCount,
                              int totalElixirs,
                              int matchCount,
                              List<Elixir> elixirs) {
        private JsonOutput(List<String> ingredients,
                           List<Elixir> elixirs,
                           int totalElixirs,
                           int matchCount,
                           boolean includeInputCount) {
            this(ingredients == null ? List.of() : List.copyOf(ingredients),
                    includeInputCount && ingredients != null ? ingredients.size() : 0,
                    totalElixirs,
                    matchCount,
                    elixirs == null ? List.of() : List.copyOf(elixirs));
        }
    }
}
