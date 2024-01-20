package com.subhadipmitra.code.module.simulation;

import com.beust.jcommander.JCommander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command-line runner for SMPP load testing.
 * Provides easy-to-use interface for performance testing.
 *
 * Usage:
 *   java -cp smpp-kafka-producer.jar com.subhadipmitra.code.module.simulation.LoadTestRunner \
 *       --host localhost --port 2775 --rate 1000 --duration 60 --connections 4
 */
public class LoadTestRunner {
    private static final Logger logger = LoggerFactory.getLogger(LoadTestRunner.class);

    public static void main(String[] args) {
        LoadTestConfig config = new LoadTestConfig();
        JCommander jc = JCommander.newBuilder()
                .addObject(config)
                .programName("LoadTestRunner")
                .build();

        try {
            jc.parse(args);
        } catch (Exception e) {
            logger.error("Error parsing arguments: {}", e.getMessage());
            jc.usage();
            System.exit(1);
        }

        if (config.isHelp()) {
            jc.usage();
            return;
        }

        logger.info("Starting load test with configuration: {}", config);

        LoadGenerator generator = new LoadGenerator(
                config.getHost(),
                config.getPort(),
                config.getSystemId(),
                config.getPassword(),
                config.getConnections(),
                config.getRate(),
                config.getDuration()
        );

        // Register shutdown hook for graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Received shutdown signal, stopping load generator...");
            generator.stop();
        }));

        try {
            generator.start();
            logger.info("Load test completed successfully");
        } catch (Exception e) {
            logger.error("Load test failed", e);
            System.exit(1);
        }
    }
}
