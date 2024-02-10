package io.smppgateway.benchmark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smppgateway.models.SRC_MapSMPP;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark focusing on latency measurements.
 * Measures p50, p95, p99 latencies for critical operations.
 */
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(value = 2, jvmArgs = {"-Xms512m", "-Xmx1024m"})
public class LatencyBenchmark {

    private ObjectMapper objectMapper;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd.HH:mm:ss.SSS");

    @Setup(Level.Trial)
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    @Benchmark
    public void messageCreationLatency(Blackhole bh) {
        String uuid = UUID.randomUUID().toString();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String msisdn = "+12025551234";
        String keyword = "TESTKEY";
        String value = "TESTVAL";
        String payload = msisdn + "," + keyword + "," + value + ",," + timestamp;

        SRC_MapSMPP message = new SRC_MapSMPP(
                uuid,
                "TR_SMPP",
                payload,
                System.currentTimeMillis(),
                "FALSE",
                UUID.randomUUID().toString(),
                "FALSE",
                msisdn,
                keyword,
                value,
                "",
                timestamp
        );
        bh.consume(message);
    }

    @Benchmark
    public void fullPipelineLatency(Blackhole bh) throws JsonProcessingException {
        // Simulate complete message processing pipeline (sans I/O)

        // 1. Generate IDs
        String uuid = UUID.randomUUID().toString();
        String batchId = UUID.randomUUID().toString();

        // 2. Create timestamp
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        // 3. Construct payload
        String msisdn = "+12025551234";
        String keyword = "BENCHMARK";
        String value = "12345";
        String payload = msisdn + "," + keyword + "," + value + ",," + timestamp;

        // 4. Create message object
        SRC_MapSMPP message = new SRC_MapSMPP(
                uuid,
                "TR_SMPP",
                payload,
                System.currentTimeMillis(),
                "FALSE",
                batchId,
                "FALSE",
                msisdn,
                keyword,
                value,
                "",
                timestamp
        );

        // 5. Serialize to JSON
        String json = objectMapper.writeValueAsString(message);
        bh.consume(json);
    }

    @Benchmark
    public void queueTimingLatency(Blackhole bh) {
        // Simulate queue timing operations
        long queueStartTime = System.nanoTime();

        // Simulate minimal processing
        String uuid = UUID.randomUUID().toString();
        bh.consume(uuid);

        long queueEndTime = System.nanoTime();
        long queueWaitTime = queueEndTime - queueStartTime;
        bh.consume(queueWaitTime);
    }

    /**
     * Run benchmarks standalone.
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(LatencyBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
