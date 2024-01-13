package com.subhadipmitra.code.module.init;

import com.subhadipmitra.code.module.events.service.completionservice.CompletionServiceProvider;
import com.subhadipmitra.code.module.metrics.MetricsServer;
import com.subhadipmitra.code.module.producer.source.SMPPProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shutdown hook to gracefully cleanup resources.
 */
public class ShutDownCleanup extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ShutDownCleanup.class);

    private final ServerMain server;
    private final SMPPProducer smppProducer;
    private final MetricsServer metricsServer;

    ShutDownCleanup(ServerMain server, SMPPProducer smppProducer, MetricsServer metricsServer) {
        this.server = server;
        this.smppProducer = smppProducer;
        this.metricsServer = metricsServer;
    }

    @Override
    public void run() {
        logger.info("Received shutdown signal..");

        // Stop SMPP server
        try {
            server.stop();
            server.printMetrics();
            server.destroy();
        } catch (Exception e) {
            logger.error("Error during server shutdown", e);
        }

        // Shutdown completion service
        CompletionServiceProvider.shutdown();

        // Shutdown Kafka producer
        smppProducer.shutdown();

        // Stop metrics server
        if (metricsServer != null) {
            metricsServer.stop();
        }

        logger.info("Finished pre-shutdown tasks");
    }
}
