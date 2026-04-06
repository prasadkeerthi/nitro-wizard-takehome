# Tasks

1. Setup & Structure
   - Create package structure under `src/main/java`:
     - `.../command`, `.../service`, `.../model`, `.../client`, `.../config`, `.../util`, `.../dto`
   - Verify `pom.xml` includes required dependencies (HTTP client, JSON parser, test libs).
   - After completing this section, run an end-to-end (E2E) smoke test for the current stage.

2. Domain & Utilities
   - Implement domain models: `Ingredient`, `Elixir` with null-safe fields.
   - Add normalization utilities:
     - lowercase
     - trim whitespace
     - collapse multiple spaces
   - Add optional synonym mapping (configurable).
   - After completing this section, run an E2E test for the current stage.

3. Client Integration
   - Implement `WizardWorldClient`:
     - Fetch elixirs and ingredients.
     - Parse JSON into domain models.
     - Add timeouts and 1–2 retries with backoff.
     - Clear error messages on failure.
   - After completing this section, run an E2E test for the current stage.

4. Application Service
   - Implement `ElixirFinderService`:
     - Normalize user input and API ingredient names.
     - Build available ingredient set.
     - Match elixirs where all required ingredients are present.
     - Sort results alphabetically.
   - After completing this section, run an E2E test for the current stage.

5. Command (CLI)
   - Build CLI input parsing (`--ingredients`, optional prompt).
   - Add `--help` usage output.
   - Add `--verbose` to print diagnostics (API counts, match totals).
   - Implement output formatting (plain text, optional JSON).
   - After completing this section, run an E2E test for the current stage.

6. Error Handling & Logging
   - Handle empty input and missing data gracefully.
   - Log INFO/ERROR with optional verbose mode.
   - After completing this section, run an E2E test for the current stage.

7. Tests
   - Unit tests: normalization and matching logic.
   - Mocked integration tests: API client behavior.
   - CLI tests: parsing and output formatting.
   - After completing this section, run an E2E test for the current stage.

8. Documentation
   - Write `README`:
     - Project overview and setup.
     - Usage examples.
     - Notes on API dependency and error cases.
   - After completing this section, run an E2E test for the current stage.

9. Production Readiness
   - Add configuration for timeouts, retries, and base URL via env vars or config file.
   - Add structured logging with clear levels and timestamps.
   - Ensure graceful shutdown on errors and non-zero exit codes.
   - Validate input strictly and surface actionable error messages.
   - Add a simple health-check mode (e.g., `--ping`) to validate API reachability.
   - After completing this section, run an E2E test for the current stage.
