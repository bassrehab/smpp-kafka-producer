package com.subhadipmitra.code.module.http.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subhadipmitra.code.module.common.utilities.UniqueId;
import com.subhadipmitra.code.module.http.model.SmsRequest;
import com.subhadipmitra.code.module.http.model.SmsResponse;
import com.subhadipmitra.code.module.metrics.MetricsRegistry;
import com.subhadipmitra.code.module.models.SMS;
import com.subhadipmitra.code.module.producer.source.SMPPProducer;
import io.micrometer.core.instrument.Timer;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * HTTP API handler for SMS submission via REST.
 * Provides 5G-compatible HTTP/2 interface as an alternative to SMPP.
 */
public class SmsApiHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(SmsApiHandler.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd.HH:mm:ss.SSS");

    private final ObjectMapper objectMapper;
    private final SMPPProducer producer;
    private final MetricsRegistry metrics;

    public SmsApiHandler(SMPPProducer producer) {
        this.objectMapper = new ObjectMapper();
        this.producer = producer;
        this.metrics = MetricsRegistry.getInstance();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        String method = exchange.getRequestMethod().toString();
        String path = exchange.getRequestPath();

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

        try {
            if ("POST".equals(method) && "/api/v1/sms".equals(path)) {
                handleSmsSubmission(exchange);
            } else if ("POST".equals(method) && "/api/v1/sms/batch".equals(path)) {
                handleBatchSubmission(exchange);
            } else if ("GET".equals(method) && "/api/v1/sms/status".equals(path)) {
                handleStatusQuery(exchange);
            } else {
                sendError(exchange, StatusCodes.NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            logger.error("Error handling request: {} {}", method, path, e);
            sendError(exchange, StatusCodes.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    /**
     * Handle single SMS submission.
     * POST /api/v1/sms
     */
    private void handleSmsSubmission(HttpServerExchange exchange) throws Exception {
        Timer.Sample timer = metrics.startHttpTimer();

        try {
            exchange.startBlocking();
            String requestBody = new BufferedReader(new InputStreamReader(exchange.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            SmsRequest request = objectMapper.readValue(requestBody, SmsRequest.class);

            // Validate request
            if (request.getDestination() == null || request.getDestination().isEmpty()) {
                sendError(exchange, StatusCodes.BAD_REQUEST, "Destination is required");
                return;
            }

            if (request.getMessage() == null && request.getKeyword() == null) {
                sendError(exchange, StatusCodes.BAD_REQUEST, "Message or keyword is required");
                return;
            }

            // Parse CSV message if needed
            request.parseMessageIfCsv();

            // Generate message ID
            String messageId = new UniqueId().getUuid();
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

            // Create SMS object for Kafka
            String payload = request.getDestination() + "," +
                    (request.getKeyword() != null ? request.getKeyword() : "") + "," +
                    (request.getValue() != null ? request.getValue() : "") + "," +
                    (request.getExtraData() != null ? request.getExtraData() : "") + "," +
                    timestamp;

            SMS sms = new SMS(
                    request.getDestination(),
                    request.getKeyword() != null ? request.getKeyword() : "",
                    request.getValue() != null ? request.getValue() : "",
                    request.getExtraData() != null ? request.getExtraData() : "",
                    timestamp,
                    "FALSE",
                    payload
            );

            // Send to Kafka
            producer.sendMessage(sms);

            // Update metrics
            metrics.incrementHttpReceived();
            metrics.recordHttpProcessingTime(timer);

            // Send success response
            SmsResponse response = SmsResponse.success(messageId, timestamp);
            exchange.setStatusCode(StatusCodes.ACCEPTED);
            exchange.getResponseSender().send(objectMapper.writeValueAsString(response));

            logger.debug("SMS submitted via HTTP API: messageId={}, destination={}",
                    messageId, request.getDestination());

        } catch (Exception e) {
            metrics.incrementHttpFailed();
            throw e;
        }
    }

    /**
     * Handle batch SMS submission.
     * POST /api/v1/sms/batch
     */
    private void handleBatchSubmission(HttpServerExchange exchange) throws Exception {
        Timer.Sample timer = metrics.startHttpTimer();

        try {
            exchange.startBlocking();
            String requestBody = new BufferedReader(new InputStreamReader(exchange.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            SmsRequest[] requests = objectMapper.readValue(requestBody, SmsRequest[].class);
            int successCount = 0;
            int failCount = 0;

            for (SmsRequest request : requests) {
                try {
                    if (request.getDestination() == null || request.getDestination().isEmpty()) {
                        failCount++;
                        continue;
                    }

                    request.parseMessageIfCsv();
                    String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

                    String payload = request.getDestination() + "," +
                            (request.getKeyword() != null ? request.getKeyword() : "") + "," +
                            (request.getValue() != null ? request.getValue() : "") + "," +
                            (request.getExtraData() != null ? request.getExtraData() : "") + "," +
                            timestamp;

                    SMS sms = new SMS(
                            request.getDestination(),
                            request.getKeyword() != null ? request.getKeyword() : "",
                            request.getValue() != null ? request.getValue() : "",
                            request.getExtraData() != null ? request.getExtraData() : "",
                            timestamp,
                            "FALSE",
                            payload
                    );

                    producer.sendMessage(sms);
                    successCount++;
                    metrics.incrementHttpReceived();

                } catch (Exception e) {
                    failCount++;
                    logger.debug("Failed to process batch item: {}", e.getMessage());
                }
            }

            metrics.recordHttpProcessingTime(timer);

            String responseJson = String.format(
                    "{\"status\":\"COMPLETED\",\"total\":%d,\"success\":%d,\"failed\":%d}",
                    requests.length, successCount, failCount);

            exchange.setStatusCode(StatusCodes.OK);
            exchange.getResponseSender().send(responseJson);

            logger.debug("Batch SMS submitted via HTTP API: total={}, success={}, failed={}",
                    requests.length, successCount, failCount);

        } catch (Exception e) {
            metrics.incrementHttpFailed();
            throw e;
        }
    }

    /**
     * Handle message status query.
     * GET /api/v1/sms/status?messageId=xxx
     */
    private void handleStatusQuery(HttpServerExchange exchange) {
        String messageId = exchange.getQueryParameters()
                .getOrDefault("messageId", new java.util.ArrayDeque<>())
                .peek();

        if (messageId == null || messageId.isEmpty()) {
            sendError(exchange, StatusCodes.BAD_REQUEST, "messageId parameter is required");
            return;
        }

        // Note: Full status tracking would require a database or cache
        // This is a placeholder implementation
        String responseJson = String.format(
                "{\"messageId\":\"%s\",\"status\":\"SUBMITTED\",\"message\":\"Status tracking not implemented\"}",
                messageId);

        exchange.setStatusCode(StatusCodes.OK);
        exchange.getResponseSender().send(responseJson);
    }

    private void sendError(HttpServerExchange exchange, int statusCode, String message) {
        try {
            SmsResponse response = SmsResponse.error(statusCode, message);
            exchange.setStatusCode(statusCode);
            exchange.getResponseSender().send(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            logger.error("Error sending error response", e);
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.getResponseSender().send("{\"error\":\"Internal server error\"}");
        }
    }
}
