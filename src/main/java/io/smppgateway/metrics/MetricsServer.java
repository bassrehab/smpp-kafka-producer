package io.smppgateway.metrics;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lightweight HTTP server for metrics and health endpoints.
 * Uses Undertow for minimal overhead.
 *
 * Endpoints:
 * - GET /metrics     - Prometheus metrics
 * - GET /health      - Health check
 * - GET /health/live - Liveness probe
 * - GET /health/ready - Readiness probe
 */
public class MetricsServer {
    private static final Logger logger = LoggerFactory.getLogger(MetricsServer.class);

    private final int port;
    private final String host;
    private Undertow server;
    private final Instant startTime;

    // Health status flags
    private final AtomicBoolean kafkaHealthy = new AtomicBoolean(true);
    private final AtomicBoolean smppHealthy = new AtomicBoolean(true);

    public MetricsServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.startTime = Instant.now();
    }

    public MetricsServer(int port) {
        this("0.0.0.0", port);
    }

    public void start() {
        server = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(this::handleRequest)
                .build();

        server.start();
        logger.info("Metrics server started on {}:{}", host, port);
        logger.info("  - Prometheus metrics: http://{}:{}/metrics", host, port);
        logger.info("  - Health check: http://{}:{}/health", host, port);
    }

    public void stop() {
        if (server != null) {
            server.stop();
            logger.info("Metrics server stopped");
        }
    }

    private void handleRequest(HttpServerExchange exchange) {
        String path = exchange.getRequestPath();
        String method = exchange.getRequestMethod().toString();

        if (!"GET".equals(method)) {
            sendResponse(exchange, StatusCodes.METHOD_NOT_ALLOWED, "text/plain", "Method not allowed");
            return;
        }

        switch (path) {
            case "/metrics" -> handleMetrics(exchange);
            case "/health" -> handleHealth(exchange);
            case "/health/live" -> handleLiveness(exchange);
            case "/health/ready" -> handleReadiness(exchange);
            default -> sendResponse(exchange, StatusCodes.NOT_FOUND, "text/plain", "Not found");
        }
    }

    private void handleMetrics(HttpServerExchange exchange) {
        String metrics = MetricsRegistry.getInstance().scrape();
        sendResponse(exchange, StatusCodes.OK, "text/plain; version=0.0.4; charset=utf-8", metrics);
    }

    private void handleHealth(HttpServerExchange exchange) {
        boolean healthy = kafkaHealthy.get() && smppHealthy.get();
        long uptimeSeconds = ChronoUnit.SECONDS.between(startTime, Instant.now());

        String json = String.format("""
                {
                  "status": "%s",
                  "uptime_seconds": %d,
                  "components": {
                    "kafka": {"status": "%s"},
                    "smpp": {"status": "%s"}
                  }
                }""",
                healthy ? "UP" : "DOWN",
                uptimeSeconds,
                kafkaHealthy.get() ? "UP" : "DOWN",
                smppHealthy.get() ? "UP" : "DOWN"
        );

        int status = healthy ? StatusCodes.OK : StatusCodes.SERVICE_UNAVAILABLE;
        sendResponse(exchange, status, "application/json", json);
    }

    private void handleLiveness(HttpServerExchange exchange) {
        // Liveness: is the application running?
        String json = """
                {"status": "UP"}""";
        sendResponse(exchange, StatusCodes.OK, "application/json", json);
    }

    private void handleReadiness(HttpServerExchange exchange) {
        // Readiness: can the application accept traffic?
        boolean ready = kafkaHealthy.get() && smppHealthy.get();
        String json = String.format("""
                {"status": "%s"}""", ready ? "UP" : "DOWN");

        int status = ready ? StatusCodes.OK : StatusCodes.SERVICE_UNAVAILABLE;
        sendResponse(exchange, status, "application/json", json);
    }

    private void sendResponse(HttpServerExchange exchange, int statusCode, String contentType, String body) {
        exchange.setStatusCode(statusCode);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType);
        exchange.getResponseSender().send(body);
    }

    // Health status setters (called by producers/consumers)
    public void setKafkaHealthy(boolean healthy) {
        kafkaHealthy.set(healthy);
    }

    public void setSmppHealthy(boolean healthy) {
        smppHealthy.set(healthy);
    }

    public boolean isKafkaHealthy() {
        return kafkaHealthy.get();
    }

    public boolean isSmppHealthy() {
        return smppHealthy.get();
    }

    public int getPort() {
        return port;
    }
}
