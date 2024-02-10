# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.0] - 2024-02-03

### Added
- HTTP/2 REST API for 5G SMSF compatibility
- POST /api/v1/sms endpoint for single SMS submission
- POST /api/v1/sms/batch endpoint for batch SMS submission
- GET /api/v1/sms/status endpoint for message tracking
- HTTP API metrics (received, failed, processing time)
- Configurable HTTP API port (default 8080)

### Changed
- Package renamed from `com.subhadipmitra.code.module` to `io.smppgateway`
- Version bumped to 2.1.0

## [2.0.4] - 2024-01-27

### Added
- Multi-stage Dockerfile with JRE 17 Alpine runtime
- docker-compose.yml with Kafka, Zookeeper, Prometheus, Grafana
- Kubernetes manifests (Deployment, Service, ConfigMap, HPA)
- Kustomize configuration for easy deployment
- Pod anti-affinity for high availability
- HorizontalPodAutoscaler for auto-scaling
- Prometheus scrape configuration
- Grafana datasource provisioning
- .dockerignore for efficient builds

### Changed
- Health check endpoints integrated with Docker/K8s probes

## [2.0.3] - 2024-01-20

### Added
- JMH (Java Microbenchmark Harness) benchmarks
- SMSSerializationBenchmark for JSON throughput testing
- ThroughputBenchmark for batch processing performance
- LatencyBenchmark for p50/p95/p99 latency measurement
- LoadGenerator for SMPP traffic simulation
- LoadTestRunner CLI for load testing
- Maven 'benchmark' profile with shade plugin

## [2.0.2] - 2024-01-13

### Added
- Micrometer metrics integration
- Prometheus-compatible metrics endpoint (/metrics)
- Health check endpoints (/health, /health/live, /health/ready)
- SMPP metrics (received, processed, failed, processing time)
- Kafka metrics (sent, failed, send time)
- Queue metrics (size, wait time)
- Session metrics (active sessions)
- JVM metrics (memory, GC, threads)
- Undertow lightweight HTTP server for metrics

### Changed
- Metrics server configurable via metrics.server.port (default 9090)

## [2.0.1] - 2024-01-06

### Security
- Upgraded Java from 8 to 17 LTS
- Fixed Log4j CVE-2021-44228 vulnerability (upgraded to 2.24.3)
- Upgraded SLF4J from 2.0.0-alpha1 to 2.0.16 stable
- Upgraded Spring Framework from 5.3.x to 6.1.14
- Upgraded Kafka clients from 2.4.1 to 3.9.0
- Upgraded Jackson from 2.12.x to 2.18.2
- Upgraded Guava from 28.0 to 33.3.1-jre
- Replaced javax.annotation with jakarta.annotation

### Changed
- Replaced all printStackTrace() with proper SLF4J logging
- Fixed thread-safety issues with AtomicLong counters
- Added try-with-resources for ConfigLoader
- Replaced SimpleDateFormat with java.time.DateTimeFormatter
- Migrated tests from JUnit 4 to JUnit 5
- Updated Maven plugins to latest versions

### Fixed
- Thread-safety issues in metrics counters
- Resource leaks in configuration loading
- Potential NPE in SMS parsing

## [1.0.0] - 2017-09-01

### Added
- Initial SMPP-to-Kafka bridge implementation
- Cloudhopper SMPP server integration
- Kafka producer for SMS messages
- Telemetry producer for metrics
- Spring XML configuration support
- Configurable thread pools
- Delivery receipt support
