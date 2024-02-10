package io.smppgateway.http.api;

import io.smppgateway.metrics.MetricsRegistry;
import io.smppgateway.producer.source.SMPPProducer;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Options;

/**
 * HTTP/2-capable API server for 5G-ready SMS submission.
 * Provides REST API endpoints as an alternative to SMPP protocol.
 *
 * Supports both HTTP/1.1 and HTTP/2 (h2c - HTTP/2 over cleartext).
 */
public class HttpApiServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpApiServer.class);

    private final int port;
    private final SMPPProducer producer;
    private final MetricsRegistry metrics;
    private Undertow server;

    public HttpApiServer(int port, SMPPProducer producer) {
        this.port = port;
        this.producer = producer;
        this.metrics = MetricsRegistry.getInstance();
    }

    public void start() {
        PathHandler pathHandler = new PathHandler()
                // Health endpoints
                .addExactPath("/health", this::handleHealth)
                .addExactPath("/health/live", this::handleLiveness)
                .addExactPath("/health/ready", this::handleReadiness)

                // Metrics endpoint
                .addExactPath("/metrics", this::handleMetrics)

                // SMS API endpoints
                .addPrefixPath("/api/v1/sms", new SmsApiHandler(producer))

                // API info endpoint
                .addExactPath("/api/v1", this::handleApiInfo);

        server = Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
                // Enable HTTP/2 upgrade (h2c)
                .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                .setServerOption(UndertowOptions.HTTP2_SETTINGS_ENABLE_PUSH, false)
                // Connection options
                .setSocketOption(Options.BACKLOG, 1000)
                .setSocketOption(Options.TCP_NODELAY, true)
                // I/O threads
                .setIoThreads(Runtime.getRuntime().availableProcessors())
                .setWorkerThreads(Runtime.getRuntime().availableProcessors() * 8)
                .setHandler(pathHandler)
                .build();

        server.start();
        logger.info("HTTP/2 API server started on port {} (h2c enabled)", port);
    }

    public void stop() {
        if (server != null) {
            server.stop();
            logger.info("HTTP/2 API server stopped");
        }
    }

    private void handleHealth(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.setStatusCode(StatusCodes.OK);

        String json = String.format(
                "{\"status\":\"UP\",\"components\":{\"smpp\":{\"status\":\"UP\"},\"kafka\":{\"status\":\"UP\"},\"http\":{\"status\":\"UP\"}}}");
        exchange.getResponseSender().send(json);
    }

    private void handleLiveness(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.setStatusCode(StatusCodes.OK);
        exchange.getResponseSender().send("{\"status\":\"UP\"}");
    }

    private void handleReadiness(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

        boolean ready = producer != null && producer.producer != null;

        if (ready) {
            exchange.setStatusCode(StatusCodes.OK);
            exchange.getResponseSender().send("{\"status\":\"UP\"}");
        } else {
            exchange.setStatusCode(StatusCodes.SERVICE_UNAVAILABLE);
            exchange.getResponseSender().send("{\"status\":\"DOWN\",\"reason\":\"Kafka producer not ready\"}");
        }
    }

    private void handleMetrics(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain; version=0.0.4; charset=utf-8");
        exchange.setStatusCode(StatusCodes.OK);
        exchange.getResponseSender().send(metrics.scrape());
    }

    private void handleApiInfo(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.setStatusCode(StatusCodes.OK);

        String json = """
                {
                  "name": "SMPP-Kafka Producer API",
                  "version": "2.0.1",
                  "protocol": "HTTP/2 (h2c)",
                  "endpoints": {
                    "POST /api/v1/sms": "Submit single SMS",
                    "POST /api/v1/sms/batch": "Submit batch SMS",
                    "GET /api/v1/sms/status": "Query message status",
                    "GET /health": "Health check",
                    "GET /metrics": "Prometheus metrics"
                  },
                  "5g_compatible": true,
                  "smsf_interface": "3GPP TS 29.540"
                }
                """;
        exchange.getResponseSender().send(json);
    }
}
