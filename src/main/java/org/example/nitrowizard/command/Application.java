package org.example.nitrowizard.command;

import org.example.nitrowizard.client.WizardWorldClient;
import org.example.nitrowizard.config.SynonymConfig;
import org.example.nitrowizard.service.ElixirFinderService;
import org.example.nitrowizard.util.InputValidator;
import org.example.nitrowizard.util.LoggingConfig;
import org.example.nitrowizard.util.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;

public final class Application {
    private Application() {
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
        CommandParseResult parseResult = CommandParser.parse(args);
        if (parseResult.showHelp()) {
            printUsage(out);
            return 0;
        }
        if (parseResult.hasError()) {
            err.println(parseResult.errorMessage());
            printUsage(out);
            return 2;
        }

        CommandOptions options = parseResult.options();
        LoggingConfig.configure(options.verbose());
        Logger logger = LoggerFactory.getLogger(Application.class);

        if (options.needsIngredients() && options.ingredients().isEmpty()) {
            if (System.console() != null) {
                out.print("Enter ingredients (comma-separated): ");
                String line = System.console().readLine();
                options = options.withIngredients(CommandParser.splitIngredients(line));
            }
        }

        if (options.needsIngredients()) {
            ValidationResult validation = InputValidator.validateIngredients(options.ingredients());
            if (!validation.isValid()) {
                err.println(validation.errorMessage());
                printUsage(out);
                return 2;
            }
            options = options.withIngredients(validation.cleanedIngredients());
        }

        return CommandExecutor.execute(options, service, client, out, err, logger);
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
}
