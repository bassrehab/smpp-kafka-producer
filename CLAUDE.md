# CLAUDE.md

## XRITICAL: Never commit or add to .gitignore CLAUDE.md file.

## CRITICAL: Code Humanization

Before any code release or publication, you MUST follow the instructions in RELEASE.md to audit and humanize the codebase. This is non-negotiable for production releases. AI-generated code patterns must be removed or obscured before publishing.



## Project Overview

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Requirements

- **Java 21+** (uses virtual threads)
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

1. **SMPP Server** (`SmscServer`) - smpp-core based server with virtual threads accepting SMPP connections
2. **Server Handler** (`SmscSmppServerHandler`) - Processes incoming `SubmitSm` PDUs, handles authentication
3. **Events Producer/Consumer** - Internal queue system using `CompletionService` for async processing
4. **Kafka Producer** (`SMPPProducer`) - Publishes SMS as JSON to Kafka topics; `TelemetryProducer` sends periodic metrics

### Key Packages

- `io.smppgateway.init` - Application entry point (`Main`), server lifecycle (`ServerMain`)
- `io.smppgateway.server` - SMPP server implementation and PDU handling
- `io.smppgateway.producer` - Kafka producers for SMS data and telemetry
- `io.smppgateway.events.service` - Internal event queue (producer/consumer pattern)
- `io.smppgateway.controller` - Message factories, delivery receipt scheduling, rate control
- `io.smppgateway.config` - Configuration loading from external properties

### Configuration Files (in settings/)

- `config.properties` - Kafka connection settings, thread pool sizes, SMPP server timeouts
- `context.xml` - Spring XML config for SMPP server (port, system ID, window settings, message factories)
- `log4j2.xml` - Log4j 2.x logging configuration

### Key Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| **smpp-core** | 1.0.1 | Modern SMPP protocol library with virtual threads |
| **Spring Framework** | 6.1.x | IoC container, configuration |
| **Kafka Clients** | 3.9.x | Kafka producer API |
| **Micrometer** | 1.14.x | Metrics collection |
| **Undertow** | 2.3.x | Lightweight HTTP server |
| **Log4j 2** | 2.24.x | Logging framework |
| **SLF4J** | 2.0.x | Logging facade |
| **Jackson** | 2.18.x | JSON serialization |
| **JCommander** | 1.82 | Command-line argument parsing |
| **JUnit 5** | 5.11.x | Testing framework |

### Test Mode

`ServerMain.isTestMode` controls whether messages are sent to Kafka. Tests use Spring test context with `/context.xml` on classpath (`src/test/resources/`).

## Metrics & Health Endpoints

The application exposes Prometheus-compatible metrics and health endpoints on port 9090 (configurable via `metrics.server.port`).

| Endpoint | Description |
|----------|-------------|
| `GET /metrics` | Prometheus metrics (SMPP, Kafka, JVM) |
| `GET /health` | Full health status with component details |
| `GET /health/live` | Kubernetes liveness probe |
| `GET /health/ready` | Kubernetes readiness probe |

### Key Metrics

- `smpp_messages_received_total` - Total SMPP messages received
- `smpp_messages_processed_total` - Successfully processed messages
- `smpp_processing_duration_seconds` - Message processing latency
- `kafka_messages_sent_total` - Messages sent to Kafka
- `kafka_send_duration_seconds` - Kafka send latency
- `smpp_queue_size` - Current queue depth
- `smpp_active_sessions` - Active SMPP sessions

## Benchmarking & Performance Testing

### JMH Benchmarks

The project includes JMH (Java Microbenchmark Harness) benchmarks for measuring performance of critical operations.

```bash
# Build benchmarks jar
mvn clean package -Pbenchmark

# Run all benchmarks
java -jar target/benchmarks.jar

# Run specific benchmark
java -jar target/benchmarks.jar SMSSerializationBenchmark

# Run with custom iterations
java -jar target/benchmarks.jar -wi 5 -i 10 -f 2
```

#### Available Benchmarks

| Benchmark | Description |
|-----------|-------------|
| `SMSSerializationBenchmark` | JSON serialization/deserialization throughput |
| `ThroughputBenchmark` | Message processing throughput at various batch sizes |
| `LatencyBenchmark` | Latency distribution (p50, p95, p99) |

### Load Testing

Use the built-in load generator to simulate SMPP traffic:

```bash
java -cp smpp-kafka-producer-*.jar \
    io.smppgateway.simulation.LoadTestRunner \
    --host localhost \
    --port 2775 \
    --rate 1000 \
    --duration 60 \
    --connections 4
```

#### Load Test Options

| Option | Description | Default |
|--------|-------------|---------|
| `-h, --host` | SMPP server host | localhost |
| `-p, --port` | SMPP server port | 2775 |
| `-s, --system-id` | SMPP system ID | smppclient1 |
| `--password` | SMPP password | password |
| `-c, --connections` | Concurrent connections | 2 |
| `-r, --rate` | Messages per second | 100 |
| `-d, --duration` | Test duration (seconds) | 60 |

## Security Notes

- All dependencies are kept up-to-date to avoid known vulnerabilities
- Credentials should be managed via environment variables or secrets management (not in config files)
- TLS/SSL should be enabled for production Kafka connections
