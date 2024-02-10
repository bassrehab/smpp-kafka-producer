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
 * JMH Benchmark for SMS serialization performance.
 * Tests JSON serialization throughput which is critical for Kafka producer performance.
 */
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 2, jvmArgs = {"-Xms512m", "-Xmx1024m"})
public class SMSSerializationBenchmark {

    private ObjectMapper objectMapper;
    private SRC_MapSMPP sampleMessage;
    private String sampleJson;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd.HH:mm:ss.SSS");

    @Setup(Level.Trial)
    public void setup() {
        objectMapper = new ObjectMapper();

        // Create a representative sample message
        String uuid = UUID.randomUUID().toString();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        sampleMessage = new SRC_MapSMPP(
                uuid,
                "TR_SMPP",
                "+1234567890,KEYWORD,VALUE,EXTRA," + timestamp,
                System.currentTimeMillis(),
                "FALSE",
                UUID.randomUUID().toString(),
                "FALSE",
                "+1234567890",
                "KEYWORD",
                "VALUE",
                "EXTRA",
                timestamp
        );

        try {
            sampleJson = objectMapper.writeValueAsString(sampleMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize sample message", e);
        }
    }

    @Benchmark
    public void serializeToJson(Blackhole bh) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(sampleMessage);
        bh.consume(json);
    }

    @Benchmark
    public void deserializeFromJson(Blackhole bh) throws JsonProcessingException {
        SRC_MapSMPP msg = objectMapper.readValue(sampleJson, SRC_MapSMPP.class);
        bh.consume(msg);
    }

    @Benchmark
    public void roundTripSerialization(Blackhole bh) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(sampleMessage);
        SRC_MapSMPP msg = objectMapper.readValue(json, SRC_MapSMPP.class);
        bh.consume(msg);
    }

    @Benchmark
    public void uuidGeneration(Blackhole bh) {
        String uuid = UUID.randomUUID().toString();
        bh.consume(uuid);
    }

    @Benchmark
    public void timestampFormatting(Blackhole bh) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        bh.consume(timestamp);
    }

    /**
     * Run benchmarks standalone.
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SMSSerializationBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
