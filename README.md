# SMPP Gateway

<p align="center">
  <strong>High-performance SMPP-to-Kafka bridge with 5G-ready HTTP/2 API</strong>
</p>

<p align="center">
  <a href="https://smppgateway.io">Website</a> •
  <a href="https://docs.smppgateway.io/smpp-kafka-producer/">Documentation</a> •
  <a href="https://docs.smppgateway.io/smpp-kafka-producer/http-api/">API Reference</a>
</p>

<p align="center">
  <a href="https://github.com/bassrehab/smpp-kafka-producer/actions"><img src="https://github.com/bassrehab/smpp-kafka-producer/workflows/CI/badge.svg" alt="Build Status"></a>
  <a href="https://openjdk.org/"><img src="https://img.shields.io/badge/Java-21+-blue.svg" alt="Java Version"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg" alt="License"></a>
  <a href="https://github.com/bassrehab/smpp-kafka-producer/releases"><img src="https://img.shields.io/github/v/release/bassrehab/smpp-kafka-producer" alt="Release"></a>
</p>

---

A production-ready SMPP-to-Kafka bridge that receives SMS messages via SMPP protocol and publishes them to Apache Kafka topics. Features include HTTP/2 REST API for 5G network compatibility, Prometheus metrics, Kubernetes-native deployment, and comprehensive benchmarking tools.

## Features

- **Dual Protocol Support**: SMPP 3.x/5.x and HTTP/2 REST API
- **5G Ready**: HTTP/2 interface compatible with 3GPP TS 29.540 SMSF
- **High Performance**: Async processing with configurable thread pools
- **Observable**: Prometheus metrics, health endpoints, Grafana dashboards
- **Cloud Native**: Docker, Kubernetes manifests, Helm-ready
- **Benchmarkable**: JMH microbenchmarks and load testing tools

## Architecture

```
                    ┌─────────────────┐
                    │   SMPP Clients  │
                    └────────┬────────┘
                             │ SMPP 3.x/5.x
                             ▼
┌─────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  HTTP/2 API │────▶│  SMPP Gateway   │────▶│  Apache Kafka   │
│  (5G SMSF)  │     │   (smpp-core)   │     │                 │
└─────────────┘     └────────┬────────┘     └─────────────────┘
                             │
                    ┌────────┴────────┐
                    │    Metrics      │
                    │  (Prometheus)   │
                    └─────────────────┘
```

## Quick Start

### Prerequisites

- Java 21+ (uses virtual threads)
- Maven 3.8+
- Apache Kafka (or use provided docker-compose)

### Build

```bash
# Clone the repository
git clone https://github.com/bassrehab/smpp-kafka-producer.git
cd smpp-kafka-producer

# Build
mvn clean package

# Output in out/smpp-producer/
```

### Run with Docker Compose

```bash
# Start Kafka, Prometheus, Grafana, and SMPP Gateway
docker-compose up -d

# View logs
docker-compose logs -f smpp-producer
```

### Run Standalone

```bash
cd out/smpp-producer

java -Xms64m -Xmx2048m \
    -Dconfig.properties=settings/config.properties \
    -Dconfig.smpp=settings/context.xml \
    -Dlog4j2.configurationFile=file:settings/log4j2.xml \
    -jar smpp-kafka-producer-2.1.0-spring-boot.jar -p 2775
```

## Configuration

### SMPP Server

The SMPP server is configured programmatically using the smpp-core fluent builder API:

```java
SmppServer server = SmppServer.builder()
    .port(2775)
    .systemId("smppserver")
    .maxConnections(100)
    .windowSize(50)
    .requestTimeout(Duration.ofSeconds(30))
    .handler(new SmscSmppServerHandler(config))
    .build();
```

### Kafka Producer (`settings/config.properties`)

```properties
source.smpp.kafka.producer.brokers=localhost:9092
source.smpp.kafka.producer.topics=TR_SMPP
source.smpp.kafka.producer.acks=all
```

### HTTP API

```properties
http.api.enabled=true
http.api.port=8080
```

## API Endpoints

### SMPP Protocol

| Port | Protocol | Description |
|------|----------|-------------|
| 2775 | SMPP | Default SMPP server port |

### HTTP/2 REST API

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/sms` | POST | Submit single SMS |
| `/api/v1/sms/batch` | POST | Submit batch SMS |
| `/api/v1/sms/status` | GET | Query message status |
| `/health` | GET | Health check |
| `/health/live` | GET | Kubernetes liveness |
| `/health/ready` | GET | Kubernetes readiness |
| `/metrics` | GET | Prometheus metrics |

### Example: Submit SMS via HTTP

```bash
curl -X POST http://localhost:8080/api/v1/sms \
  -H "Content-Type: application/json" \
  -d '{
    "destination": "+1234567890",
    "message": "KEYWORD,VALUE,EXTRA"
  }'
```

## Metrics

Prometheus metrics available at `http://localhost:9090/metrics`:

| Metric | Description |
|--------|-------------|
| `smpp_messages_received_total` | Total SMPP messages received |
| `smpp_messages_processed_total` | Successfully processed messages |
| `smpp_processing_duration_seconds` | Processing latency histogram |
| `kafka_messages_sent_total` | Messages sent to Kafka |
| `kafka_send_duration_seconds` | Kafka send latency |
| `http_messages_received_total` | HTTP API messages received |
| `smpp_queue_size` | Current queue depth |
| `smpp_active_sessions` | Active SMPP sessions |

## Kubernetes Deployment

```bash
# Deploy to Kubernetes
kubectl apply -k k8s/

# Check status
kubectl -n smpp-producer get pods

# View logs
kubectl -n smpp-producer logs -f deployment/smpp-producer
```

## Benchmarking

### JMH Microbenchmarks

```bash
# Build benchmarks
mvn clean package -Pbenchmark

# Run all benchmarks
java -jar target/benchmarks.jar

# Run specific benchmark
java -jar target/benchmarks.jar SMSSerializationBenchmark
```

### Load Testing

```bash
java -cp smpp-kafka-producer-*.jar \
    io.smppgateway.simulation.LoadTestRunner \
    --host localhost \
    --port 2775 \
    --rate 1000 \
    --duration 60 \
    --connections 4
```

## Project Structure

```
smpp-kafka-producer/
├── src/main/java/io/smppgateway/
│   ├── init/          # Application entry point
│   ├── server/        # SMPP server implementation
│   ├── http/          # HTTP/2 REST API
│   ├── producer/      # Kafka producers
│   ├── events/        # Internal event queue
│   ├── metrics/       # Prometheus metrics
│   ├── simulation/    # Load testing tools
│   └── config/        # Configuration loaders
├── settings/          # Configuration files
├── k8s/              # Kubernetes manifests
├── docker/           # Docker configurations
└── out/              # Build output
```

## Development

### Run Tests

```bash
mvn test
```

### Check Dependencies

```bash
mvn versions:display-dependency-updates
```

### Code Style

The project follows standard Java conventions with:
- 4-space indentation
- 120 character line limit
- Javadoc for public APIs

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## References

- [SMPP Protocol Specification](https://smpp.org/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [smpp-core Library](https://github.com/bassrehab/smpp-core) - Modern Java 21 SMPP library with virtual threads
- [3GPP TS 29.540 - 5G SMS](https://www.3gpp.org/specifications)
