package org.example.nitrowizard.command;

import java.util.ArrayList;
import java.util.List;

public final class CommandParser {
    private CommandParser() {
    }

    public static CommandParseResult parse(String[] args) {
        if (args == null || args.length == 0) {
            return CommandParseResult.error("No arguments provided.");
        }

        List<String> ingredients = new ArrayList<>();
        boolean verbose = false;
        OutputMode outputMode = OutputMode.TEXT;
        CommandMode mode = CommandMode.MATCH;
        boolean listElixirsFull = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--help".equals(arg) || "-h".equals(arg)) {
                return CommandParseResult.help();
            } else if ("--verbose".equals(arg) || "-v".equals(arg)) {
                verbose = true;
            } else if ("--ping".equals(arg)) {
                CommandParseResult modeResult = setMode(CommandMode.PING, mode);
                if (modeResult != null) {
                    return modeResult;
                }
                mode = CommandMode.PING;
            } else if ("--sample".equals(arg)) {
                CommandParseResult modeResult = setMode(CommandMode.SAMPLE, mode);
                if (modeResult != null) {
                    return modeResult;
                }
                mode = CommandMode.SAMPLE;
            } else if ("--list-elixirs".equals(arg)) {
                CommandParseResult modeResult = setMode(CommandMode.LIST, mode);
                if (modeResult != null) {
                    return modeResult;
                }
                mode = CommandMode.LIST;
            } else if (arg.startsWith("--list-elixirs=")) {
                CommandParseResult modeResult = setMode(CommandMode.LIST, mode);
                if (modeResult != null) {
                    return modeResult;
                }
                mode = CommandMode.LIST;
                String value = arg.substring("--list-elixirs=".length());
                if ("full".equalsIgnoreCase(value)) {
                    listElixirsFull = true;
                } else {
                    return CommandParseResult.error("Unknown list-elixirs mode: " + value);
                }
            } else if ("--json".equals(arg)) {
                outputMode = OutputMode.JSON;
            } else if ("--output".equals(arg) || "-o".equals(arg)) {
                if (i + 1 >= args.length) {
                    return CommandParseResult.error("Missing value for --output.");
                }
                String value = args[++i];
                if ("json".equalsIgnoreCase(value)) {
                    outputMode = OutputMode.JSON;
                } else if ("text".equalsIgnoreCase(value)) {
                    outputMode = OutputMode.TEXT;
                } else {
                    return CommandParseResult.error("Unknown output format: " + value);
                }
            } else if ("--ingredients".equals(arg) || "-i".equals(arg)) {
                if (i + 1 >= args.length) {
                    return CommandParseResult.error("Missing value for --ingredients.");
                }
                ingredients.addAll(splitIngredients(args[++i]));
            } else {
                return CommandParseResult.error("Unknown argument: " + arg);
            }
        }

        CommandOptions options = new CommandOptions(ingredients, outputMode, verbose, mode, listElixirsFull);
        return CommandParseResult.ok(options);
    }

    private static CommandParseResult setMode(CommandMode nextMode, CommandMode currentMode) {
        if (currentMode != CommandMode.MATCH && currentMode != nextMode) {
            return CommandParseResult.error("Choose only one mode: --ping, --sample, or --list-elixirs.");
        }
        return null;
    }

    static List<String> splitIngredients(String input) {
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
}
