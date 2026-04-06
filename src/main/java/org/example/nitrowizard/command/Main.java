package org.example.nitrowizard.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nitrowizard.service.ElixirFinderService;
import org.example.nitrowizard.service.ElixirMatchResult;
import org.example.nitrowizard.config.SynonymConfig;
import org.example.nitrowizard.model.Elixir;
import org.example.nitrowizard.client.WizardWorldClient;
import org.example.nitrowizard.util.LoggingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        WizardWorldClient client = WizardWorldClient.createDefault();
        ElixirFinderService service = new ElixirFinderService(client, new SynonymConfig());
        int exitCode = run(args, service, client, System.out, System.err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static int run(String[] args,
                   ElixirFinderService service,
                   WizardWorldClient client,
                   PrintStream out,
                   PrintStream err) {
        if (args.length == 0) {
            printUsage(out);
            return 2;
        }

        List<String> ingredients = new ArrayList<>();
        boolean showHelp = false;
        boolean verbose = false;
        OutputMode outputMode = OutputMode.TEXT;
        boolean ping = false;
        boolean sample = false;
        boolean listElixirs = false;
        boolean listElixirsFull = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--help".equals(arg) || "-h".equals(arg)) {
                showHelp = true;
            } else if ("--verbose".equals(arg) || "-v".equals(arg)) {
                verbose = true;
            } else if ("--ping".equals(arg)) {
                ping = true;
            } else if ("--sample".equals(arg)) {
                sample = true;
            } else if ("--list-elixirs".equals(arg)) {
                listElixirs = true;
            } else if (arg.startsWith("--list-elixirs=")) {
                listElixirs = true;
                String value = arg.substring("--list-elixirs=".length());
                if ("full".equalsIgnoreCase(value)) {
                    listElixirsFull = true;
                } else {
                    err.println("Unknown list-elixirs mode: " + value);
                    return 2;
                }
            } else if ("--json".equals(arg)) {
                outputMode = OutputMode.JSON;
            } else if ("--output".equals(arg) || "-o".equals(arg)) {
                if (i + 1 >= args.length) {
                    err.println("Missing value for --output.");
                    return 2;
                }
                String value = args[++i];
                if ("json".equalsIgnoreCase(value)) {
                    outputMode = OutputMode.JSON;
                } else if ("text".equalsIgnoreCase(value)) {
                    outputMode = OutputMode.TEXT;
                } else {
                    err.println("Unknown output format: " + value);
                    return 2;
                }
            } else if ("--ingredients".equals(arg) || "-i".equals(arg)) {
                if (i + 1 >= args.length) {
                    err.println("Missing value for --ingredients.");
                    return 2;
                }
                ingredients.addAll(splitIngredients(args[++i]));
            } else {
                err.println("Unknown argument: " + arg);
                return 2;
            }
        }

        if (showHelp) {
            printUsage(out);
            return 0;
        }

        LoggingConfig.configure(verbose);
        Logger logger = LoggerFactory.getLogger(Main.class);

        if (ping) {
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

        if (sample) {
            return printSample(client, outputMode, out, err, logger);
        }

        if (listElixirs) {
            return printElixirs(client, outputMode, listElixirsFull, out, err, logger);
        }

        if (ingredients.isEmpty()) {
            if (System.console() != null) {
                out.print("Enter ingredients (comma-separated): ");
                String line = System.console().readLine();
                ingredients.addAll(splitIngredients(line));
            }
            if (ingredients.isEmpty()) {
                err.println("No ingredients provided.");
                printUsage(out);
                return 2;
            }
        }

        try {
            logger.info("Searching elixirs for {} ingredient(s).", ingredients.size());
            ElixirMatchResult result = service.findElixirs(ingredients);
            List<Elixir> matches = result.getMatches();
            if (outputMode == OutputMode.JSON) {
                writeJson(out, ingredients, result, verbose);
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
            if (verbose) {
                out.println();
                out.println("Diagnostics:");
                out.println("  Input ingredients: " + ingredients.size());
                out.println("  Total elixirs fetched: " + result.getTotalElixirs());
                out.println("  Matches: " + matches.size());
            }
        } catch (IOException | InterruptedException ex) {
            logger.error("Failed to fetch elixirs.", ex);
            err.println("Failed to fetch elixirs: " + ex.getMessage());
            return 1;
        }
        return 0;
    }

    private static List<String> splitIngredients(String input) {
        if (input == null || input.isBlank()) {
            return List.of();
        }
        String[] parts = input.split(",");
        List<String> values = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                values.add(trimmed);
            }
        }
        return values;
    }

    private static void printUsage(PrintStream out) {
        out.println("Usage: nitro-wizard --ingredients \"A, B, C\" [--output text|json] [--verbose]");
        out.println("       nitro-wizard --ping");
        out.println("       nitro-wizard --sample [--output text|json]");
        out.println("       nitro-wizard --list-elixirs[=full] [--output text|json]");
        out.println("Options:");
        out.println("  -i, --ingredients  Comma-separated ingredient list");
        out.println("  -o, --output       Output format: text (default) or json");
        out.println("  --json             Shortcut for --output json");
        out.println("  -v, --verbose      Print diagnostic counts");
        out.println("  --ping             Check API reachability");
        out.println("  --sample           Print a sample elixir with ingredients");
        out.println("  --list-elixirs     List all elixirs (names only)");
        out.println("  --list-elixirs=full  Include effect and ingredients in text output");
        out.println("  -h, --help         Show this help");
    }

    private static void writeJson(PrintStream out, List<String> ingredients, ElixirMatchResult result, boolean verbose)
            throws IOException {
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

    private enum OutputMode {
        TEXT,
        JSON
    }

    private static final class JsonOutput {
        public final List<String> ingredients;
        public final int inputCount;
        public final int totalElixirs;
        public final int matchCount;
        public final List<Elixir> elixirs;

        private JsonOutput(List<String> ingredients,
                           List<Elixir> elixirs,
                           int totalElixirs,
                           int matchCount,
                           boolean includeInputCount) {
            this.ingredients = List.copyOf(ingredients);
            this.elixirs = List.copyOf(elixirs);
            this.totalElixirs = totalElixirs;
            this.matchCount = matchCount;
            this.inputCount = includeInputCount ? ingredients.size() : 0;
        }
    }
}
