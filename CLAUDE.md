# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Requirements

- **Java 17+** (LTS version recommended)
- **Maven 3.8+**

## Build Commands

```bash
# Build and package (outputs to out/smpp-producer/)
mvn clean package

# Check for dependency updates
mvn versions:display-dependency-updates

# Run tests
mvn test

# Run a single test class
mvn -Dtest=ServerMainTest test
```

## Running the Application

```bash
# After building, from out/smpp-producer/ directory:
java -Xms64m -Xmx2048m \
    -Dconfig.properties=settings/config.properties \
    -Dconfig.smpp=settings/context.xml \
    -Dlog4j2.configurationFile=file:settings/log4j2.xml \
    -jar smpp-kafka-producer-2.0.1-spring-boot.jar -p <port>

# Or use run.sh
./run.sh -p <port>
```

The `-p` flag specifies SMPP server port(s) (required, supports multiple ports).

## Architecture Overview

This is an SMPP-to-Kafka bridge that receives SMS messages via SMPP protocol and publishes them to Kafka topics.

### Core Flow

1. **SMPP Server** (`SmscServer`) - Cloudhopper-based server accepting SMPP connections on configured ports
2. **Session Handler** (`SmscSmppSessionHandler`) - Processes incoming `SubmitSm` PDUs, extracts SMS data
3. **Events Producer/Consumer** - Internal queue system using `CompletionService` for async processing
4. **Kafka Producer** (`SMPPProducer`) - Publishes SMS as JSON to Kafka topics; `TelemetryProducer` sends periodic metrics

### Key Packages

- `com.subhadipmitra.code.module.init` - Application entry point (`Main`), server lifecycle (`ServerMain`)
- `com.subhadipmitra.code.module.server` - SMPP server implementation and PDU handling
- `com.subhadipmitra.code.module.producer` - Kafka producers for SMS data and telemetry
- `com.subhadipmitra.code.module.events.service` - Internal event queue (producer/consumer pattern)
- `com.subhadipmitra.code.module.controller` - Message factories, delivery receipt scheduling, rate control
- `com.subhadipmitra.code.module.config` - Configuration loading from external properties

### Configuration Files (in settings/)

- `config.properties` - Kafka connection settings, thread pool sizes, SMPP server timeouts
- `context.xml` - Spring XML config for SMPP server (port, system ID, window settings, message factories)
- `log4j2.xml` - Log4j 2.x logging configuration

### Key Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| **Spring Framework** | 6.1.x | IoC container, configuration |
| **Kafka Clients** | 3.9.x | Kafka producer API |
| **ch-smpp (Cloudhopper)** | 5.1.x | SMPP protocol implementation |
| **Log4j 2** | 2.24.x | Logging framework |
| **SLF4J** | 2.0.x | Logging facade |
| **Jackson** | 2.18.x | JSON serialization |
| **JCommander** | 1.82 | Command-line argument parsing |
| **JUnit 5** | 5.11.x | Testing framework |

### Test Mode

`ServerMain.isTestMode` controls whether messages are sent to Kafka. Tests use Spring test context with `/context.xml` on classpath (`src/test/resources/`).

## Security Notes

- All dependencies are kept up-to-date to avoid known vulnerabilities
- Credentials should be managed via environment variables or secrets management (not in config files)
- TLS/SSL should be enabled for production Kafka connections
