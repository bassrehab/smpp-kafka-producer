package com.subhadipmitra.code.module.benchmark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subhadipmitra.code.module.models.SRC_MapSMPP;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark measuring message throughput under various batch sizes.
 * Simulates the full message processing pipeline sans Kafka I/O.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 3)
@Fork(value = 2, jvmArgs = {"-Xms1g", "-Xmx2g"})
public class ThroughputBenchmark {

    private ObjectMapper objectMapper;
    private Random random;
    private List<String> phoneNumbers;
    private List<String> keywords;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd.HH:mm:ss.SSS");

    @Param({"1", "10", "100", "1000"})
    private int batchSize;

    @Setup(Level.Trial)
    public void setup() {
        objectMapper = new ObjectMapper();
        random = new Random(42); // Fixed seed for reproducibility

        // Pre-generate test data
        phoneNumbers = new ArrayList<>(1000);
        keywords = new ArrayList<>(100);

        for (int i = 0; i < 1000; i++) {
            phoneNumbers.add("+1" + String.format("%010d", random.nextInt(Integer.MAX_VALUE)));
        }
        for (int i = 0; i < 100; i++) {
            keywords.add("KEYWORD" + i);
        }
    }

    @Benchmark
    public void processBatch(Blackhole bh) throws JsonProcessingException {
        String batchId = UUID.randomUUID().toString();
        long entryTime = System.currentTimeMillis();

        for (int i = 0; i < batchSize; i++) {
            // Simulate message creation
            String uuid = UUID.randomUUID().toString();
            String msisdn = phoneNumbers.get(random.nextInt(phoneNumbers.size()));
            String keyword = keywords.get(random.nextInt(keywords.size()));
            String value = "VALUE" + random.nextInt(1000);
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String payload = msisdn + "," + keyword + "," + value + ",," + timestamp;

            SRC_MapSMPP message = new SRC_MapSMPP(
                    uuid,
                    "TR_SMPP",
                    payload,
                    entryTime,
                    "FALSE",
                    batchId,
                    "FALSE",
                    msisdn,
                    keyword,
                    value,
                    "",
                    timestamp
            );

            // Serialize to JSON (what Kafka producer does)
            String json = objectMapper.writeValueAsString(message);
            bh.consume(json);
        }
    }

    @Benchmark
    @Threads(4)
    public void processBatchMultiThreaded(Blackhole bh) throws JsonProcessingException {
        processBatch(bh);
    }

    @Benchmark
    @Threads(8)
    public void processBatchHighConcurrency(Blackhole bh) throws JsonProcessingException {
        processBatch(bh);
    }

    /**
     * Run benchmarks standalone.
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ThroughputBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
