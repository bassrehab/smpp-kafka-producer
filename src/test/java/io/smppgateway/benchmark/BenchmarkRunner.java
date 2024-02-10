package io.smppgateway.benchmark;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Main entry point for running all JMH benchmarks.
 * Outputs results in both console and JSON format for analysis.
 *
 * Usage:
 *   mvn test-compile && java -jar target/benchmarks.jar
 *
 * Or run specific benchmarks:
 *   java -jar target/benchmarks.jar SMSSerializationBenchmark
 */
public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {
        // Default: run all benchmarks in this package
        String includes = args.length > 0 ? args[0] : ".*Benchmark.*";

        Options opt = new OptionsBuilder()
                // Include pattern
                .include(includes)

                // Warm-up configuration
                .warmupIterations(3)
                .warmupTime(TimeValue.seconds(1))

                // Measurement configuration
                .measurementIterations(5)
                .measurementTime(TimeValue.seconds(2))

                // Fork configuration
                .forks(2)

                // JVM args
                .jvmArgs("-Xms1g", "-Xmx2g", "-XX:+UseG1GC")

                // Output results to JSON file
                .resultFormat(ResultFormatType.JSON)
                .result("benchmark-results.json")

                // Enable GC profiling
                .addProfiler("gc")

                .build();

        System.out.println("=".repeat(60));
        System.out.println("SMPP-Kafka Producer Benchmark Suite");
        System.out.println("=".repeat(60));
        System.out.println();
        System.out.println("Running benchmarks matching: " + includes);
        System.out.println();

        new Runner(opt).run();

        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("Benchmark completed. Results saved to: benchmark-results.json");
        System.out.println("=".repeat(60));
    }
}
