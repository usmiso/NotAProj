# AI Agent Instructions for springboot-collab

## Project Overview
This is a **Spring Boot 4.0.0** application using Java 21, managed with Maven. The project uses a minimal starter configuration with core Spring Boot dependencies only.

## Architecture & Key Files
- **Entry Point**: `src/main/java/com/example/SpringbootCollabApplication.java` - Contains `@SpringBootApplication` main method
- **Config**: `src/main/resources/application.properties` - Application name configuration only (minimal setup)
- **Build Config**: `pom.xml` - Maven configuration with Spring Boot 4.0.0 parent POM
- **Tests**: `src/test/java/com/example/SpringbootCollabApplicationTests.java` - Basic context loading test using `@SpringBootTest`

## Developer Workflows

### Building the Project
```bash
# Via Maven wrapper (cross-platform)
./mvnw clean package

# Or on Windows
mvnw.cmd clean package
```

### Running the Application
```bash
./mvnw spring-boot:run
```

### Running Tests
```bash
./mvnw test
```

### Adding Dependencies
All dependencies go in `pom.xml` under `<dependencies>`. The project inherits from `spring-boot-starter-parent:4.0.0`, so versions are managed automatically for Spring ecosystem libraries.

## Code Patterns & Conventions

### Package Structure
- Root package: `com.example`
- Standard Maven layout with clear separation: `src/main/java` and `src/test/java`

### Spring Boot Application
- Use `@SpringBootApplication` on the main class
- Follow standard Spring Boot bootstrap pattern with `SpringApplication.run()`
- Configuration driven by `application.properties` (or `application.yml`)

### Testing
- Use `@SpringBootTest` for integration tests that load the full application context
- Tests inherit JUnit Jupiter (Jupiter) runner from `spring-boot-starter-test`
- Tests located in same package structure as source code under `src/test`

## Important Notes
- **Java Version**: This project targets Java 21. Ensure compatibility when adding dependencies.
- **Spring Boot 4.0.0**: A newer major version with modern frameworks. Check Spring documentation for any breaking changes from earlier versions.
- **Minimal Dependencies**: Currently only includes `spring-boot-starter` and `spring-boot-starter-test`. Add additional starters (web, data-jpa, etc.) as needed to `pom.xml`.

## Next Steps for Development
1. Implement controllers if building a web service (add `spring-boot-starter-web`)
2. Add database support if needed (add `spring-boot-starter-data-jpa` + database driver)
3. Expand application properties configuration as features are added
4. Keep test coverage aligned with code additions using `@SpringBootTest` pattern
