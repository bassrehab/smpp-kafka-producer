package com.subhadipmitra.code.module.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Central metrics registry for SMPP Kafka Producer.
 * Provides Prometheus-compatible metrics for monitoring and alerting.
 */
public final class MetricsRegistry {
    private static final Logger logger = LoggerFactory.getLogger(MetricsRegistry.class);

    private static volatile MetricsRegistry instance;
    private final PrometheusMeterRegistry registry;

    // SMPP Metrics
    private final Counter smppMessagesReceived;
    private final Counter smppMessagesProcessed;
    private final Counter smppMessagesFailed;
    private final Timer smppProcessingTime;

    // Kafka Metrics
    private final Counter kafkaMessagesSent;
    private final Counter kafkaMessagesFailed;
    private final Timer kafkaSendTime;

    // Queue Metrics
    private final AtomicLong queueSize = new AtomicLong(0);
    private final Timer queueWaitTime;

    // Session Metrics
    private final AtomicLong activeSessions = new AtomicLong(0);

    // HTTP API Metrics
    private final Counter httpMessagesReceived;
    private final Counter httpMessagesFailed;
    private final Timer httpProcessingTime;

    private MetricsRegistry() {
        this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        // Bind JVM metrics
        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
        new UptimeMetrics().bindTo(registry);

        // SMPP Counters
        smppMessagesReceived = Counter.builder("smpp_messages_received_total")
                .description("Total SMPP messages received")
                .register(registry);

        smppMessagesProcessed = Counter.builder("smpp_messages_processed_total")
                .description("Total SMPP messages successfully processed")
                .register(registry);

        smppMessagesFailed = Counter.builder("smpp_messages_failed_total")
                .description("Total SMPP messages that failed processing")
                .register(registry);

        smppProcessingTime = Timer.builder("smpp_processing_duration_seconds")
                .description("Time spent processing SMPP messages")
                .register(registry);

        // Kafka Counters
        kafkaMessagesSent = Counter.builder("kafka_messages_sent_total")
                .description("Total messages sent to Kafka")
                .register(registry);

        kafkaMessagesFailed = Counter.builder("kafka_messages_failed_total")
                .description("Total messages that failed to send to Kafka")
                .register(registry);

        kafkaSendTime = Timer.builder("kafka_send_duration_seconds")
                .description("Time spent sending messages to Kafka")
                .register(registry);

        // Queue Metrics
        Gauge.builder("smpp_queue_size", queueSize, AtomicLong::get)
                .description("Current size of the SMPP processing queue")
                .register(registry);

        queueWaitTime = Timer.builder("smpp_queue_wait_duration_seconds")
                .description("Time messages spend waiting in queue")
                .register(registry);

        // Session Metrics
        Gauge.builder("smpp_active_sessions", activeSessions, AtomicLong::get)
                .description("Number of active SMPP sessions")
                .register(registry);

        // HTTP API Metrics
        httpMessagesReceived = Counter.builder("http_messages_received_total")
                .description("Total messages received via HTTP API")
                .register(registry);

        httpMessagesFailed = Counter.builder("http_messages_failed_total")
                .description("Total HTTP API requests that failed")
                .register(registry);

        httpProcessingTime = Timer.builder("http_processing_duration_seconds")
                .description("Time spent processing HTTP API requests")
                .register(registry);

        logger.info("Metrics registry initialized with Prometheus support");
    }

    public static MetricsRegistry getInstance() {
        if (instance == null) {
            synchronized (MetricsRegistry.class) {
                if (instance == null) {
                    instance = new MetricsRegistry();
                }
            }
        }
        return instance;
    }

    public MeterRegistry getRegistry() {
        return registry;
    }

    public String scrape() {
        return registry.scrape();
    }

    // SMPP metric methods
    public void incrementSmppReceived() {
        smppMessagesReceived.increment();
    }

    public void incrementSmppProcessed() {
        smppMessagesProcessed.increment();
    }

    public void incrementSmppFailed() {
        smppMessagesFailed.increment();
    }

    public Timer.Sample startSmppTimer() {
        return Timer.start(registry);
    }

    public void recordSmppProcessingTime(Timer.Sample sample) {
        sample.stop(smppProcessingTime);
    }

    // Kafka metric methods
    public void incrementKafkaSent() {
        kafkaMessagesSent.increment();
    }

    public void incrementKafkaFailed() {
        kafkaMessagesFailed.increment();
    }

    public Timer.Sample startKafkaTimer() {
        return Timer.start(registry);
    }

    public void recordKafkaSendTime(Timer.Sample sample) {
        sample.stop(kafkaSendTime);
    }

    // Queue metric methods
    public void setQueueSize(long size) {
        queueSize.set(size);
    }

    public void incrementQueueSize() {
        queueSize.incrementAndGet();
    }

    public void decrementQueueSize() {
        queueSize.decrementAndGet();
    }

    public void recordQueueWaitTime(long nanos) {
        queueWaitTime.record(java.time.Duration.ofNanos(nanos));
    }

    // Session metric methods
    public void incrementActiveSessions() {
        activeSessions.incrementAndGet();
    }

    public void decrementActiveSessions() {
        activeSessions.decrementAndGet();
    }

    public long getActiveSessions() {
        return activeSessions.get();
    }

    // HTTP API metric methods
    public void incrementHttpReceived() {
        httpMessagesReceived.increment();
    }

    public void incrementHttpFailed() {
        httpMessagesFailed.increment();
    }

    public Timer.Sample startHttpTimer() {
        return Timer.start(registry);
    }

    public void recordHttpProcessingTime(Timer.Sample sample) {
        sample.stop(httpProcessingTime);
    }
}
