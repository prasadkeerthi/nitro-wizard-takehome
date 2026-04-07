package org.example.nitrowizard.command;

import java.util.List;

public record CommandOptions(List<String> ingredients,
                             OutputMode outputMode,
                             boolean verbose,
                             CommandMode mode,
                             boolean listElixirsFull) {
    public CommandOptions {
        if (ingredients == null) {
            ingredients = List.of();
        }
        if (outputMode == null) {
            outputMode = OutputMode.TEXT;
        }
        if (mode == null) {
            mode = CommandMode.MATCH;
        }
    }

    public boolean needsIngredients() {
        return mode == CommandMode.MATCH;
    }

    public CommandOptions withIngredients(List<String> newIngredients) {
        return new CommandOptions(newIngredients, outputMode, verbose, mode, listElixirsFull);
    }
}
