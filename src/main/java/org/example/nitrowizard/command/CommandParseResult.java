package org.example.nitrowizard.command;

public record CommandParseResult(CommandOptions options, String errorMessage, boolean showHelp) {
    public static CommandParseResult ok(CommandOptions options) {
        return new CommandParseResult(options, null, false);
    }

    public static CommandParseResult help() {
        return new CommandParseResult(null, null, true);
    }

    public static CommandParseResult error(String message) {
        return new CommandParseResult(null, message, false);
    }

    public boolean hasError() {
        return errorMessage != null && !errorMessage.isBlank();
    }
}
