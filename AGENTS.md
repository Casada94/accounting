# Repository Guidelines

## Project Structure & Module Organization

This is a Java 21 Spring Boot application built with Gradle. Production code lives under `src/main/java/com/asadaclan/accounting`, organized by responsibility: `controllers/` handles HTTP endpoints, `service/` contains business operations, `producers/` integrates outbound messaging, `reporting/` generates reports, and `models/`, `enums/`, `exceptions/`, and `util/` hold supporting types.

Tests mirror these packages under `src/test/java`. Runtime configuration is in `src/main/resources`; environment profiles use `dev-application.yaml`, `stage-application.yaml`, and `prod-application.yaml`. LaTeX report templates belong in `src/main/resources/templates`.

## Build, Test, and Development Commands

Use the checked-in Gradle wrapper so contributors run the expected Gradle version:

- `./gradlew bootRun --args='--spring.profiles.active=dev'` starts the service with the development profile.
- `./gradlew test` runs the JUnit test suite.
- `./gradlew build` compiles, tests, and packages the application.
- `./gradlew clean` removes generated build output.

Run `./gradlew build` before submitting a pull request.

## Coding Style & Naming Conventions

Use package names in lowercase and Java types in PascalCase. Name methods and variables in camelCase and constants in `UPPER_SNAKE_CASE`. Follow the formatting of neighboring Java files; use consistent indentation, braces on the declaration line, and focused classes. Controllers should delegate business logic to services. Name custom exceptions with an `Exception` suffix and keep shared file or cryptographic behavior in `util/`. Lombok is available, but use generated methods only when they keep model behavior clear.

## Testing Guidelines

Tests use Spring Boot Test and JUnit Platform. Place each test in the package matching its production class and follow the existing `*Tests.java` naming pattern, such as `SmsServiceTests.java`. Add unit tests for parsing, calculations, error handling, and report output; use Spring context tests only when framework wiring is relevant. Mock email, messaging, filesystem, process, and database boundaries. Tests must not depend on real credentials or production services.

## Commit & Pull Request Guidelines

No established commit convention is visible in the current history. Use short, imperative subjects such as `Validate inbound SMS transactions`, and keep commits focused. Pull requests should describe the behavior changed, note affected profiles or integrations, link relevant issues, and list verification performed. Include sample request/response data for API changes and sanitized report output for template changes.

## Security & Configuration

Never commit API keys, database credentials, encryption secrets, or customer financial data. Keep environment-specific values outside tracked YAML files and inject them through environment variables or deployment secrets. Use synthetic, anonymized fixtures in tests and examples.
