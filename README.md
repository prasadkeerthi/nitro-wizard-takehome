# Nitro Wizard

Console app that tells you which Wizard World elixirs can be brewed from a set of available ingredients.

## Features
- CLI input via `--ingredients` or interactive prompt.
- Matches elixirs by required ingredients.
- Text or JSON output.
- Configurable API endpoints and timeouts via env vars.

## Requirements
- Java 21
- Maven 3.8+

## Install
```
mvn -q test
```

## Usage
```
mvn -q exec:java -Dexec.args="--ingredients \"Boomslang Skin, Leech Juice\""
```

## Package as JAR
Build a runnable fat JAR:
```
mvn -q -DskipTests package
```

Run as a CLI:
```
java -jar target/nitro-wizard.jar --ingredients "Boomslang Skin, Leech Juice"
```

Or using the wrapper script:
```
./bin/nitro-wizard --ingredients "Boomslang Skin, Leech Juice"
```

Verbose diagnostics:
```
mvn -q exec:java -Dexec.args="--ingredients \"Boomslang Skin, Leech Juice\" --verbose"
```

JSON output:
```
mvn -q exec:java -Dexec.args="--ingredients \"Leech Juice\" --output json"
```

Sample elixir with ingredients:
```
mvn -q exec:java -Dexec.args="--sample"
```

List all elixir names:
```
mvn -q exec:java -Dexec.args="--list-elixirs"
```

List elixirs with effects and ingredients:
```
mvn -q exec:java -Dexec.args="--list-elixirs=full"
```

Health check:
```
mvn -q exec:java -Dexec.args="--ping"
```

## Configuration
Runtime settings live in `src/main/resources/config.properties`.

Example values:
```
wizard.api.baseUrl=https://wizard-world-api.herokuapp.com
wizard.api.ingredientsPath=/Ingredients
wizard.api.elixirsPath=/Elixirs
wizard.api.connectTimeoutSeconds=5
wizard.api.requestTimeoutSeconds=15
wizard.api.maxRetries=2
wizard.api.backoffMillis=250
```

## Testing
```
mvn -q test
```

## Notes
- The Wizard World API may add fields over time. The client ignores unknown fields.
- If no elixirs are found for the provided ingredients, the CLI prints a clear message and exits successfully.

## AI Usage
This project may include AI-assisted development. Be ready to discuss how tools were used if asked.
